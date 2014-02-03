(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]]))

(def app-state (atom {:text "Hello prototype!"}))
(def username "facebook1428467850")
(def password "3a34gc72")
(def url "http://8.21.28.222:5000")




(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))


(defn handler2 [response]
  (.log js/console (str response))
  )




                                        ;(om/root {:text "Hello prototype!"} widget (. js/document (getElementById "my-app")))

(def o (atom nil))

(defn handler [response]
  (.log js/console (str response))
  )

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(def connect-channel (chan))

(defn try-to-connect [e data owner]
  (let [url (.-value (om/get-node owner "url"))
        password (.-value (om/get-node owner "password"))
        username (.-value (om/get-node owner "username"))
        ]
    (swap! app-state assoc :url url :password password :username username)
    #_(.log js/console url password username)


    (GET
     "/connect"
     {:handler (fn [response] (if (:success response)
                               (put! connect-channel (:token-id response))
                               (om/root (swap! app-state assoc :text "error!") widget (. js/document (getElementById "my-app")))))
      :error-handler error-handler
      :params {:url url :password password :username username}
      :response-format :json
      :keywords? true})

    ))

(defn tenant-view [tenant owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js {:className "list-group-item"} (:name tenant)))))

(defn tenants-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (dom/h2 nil "Tenants list")
               (apply dom/ul #js {:className "list-group"}
                      (om/build-all tenant-view (:tenants app)))))))

(defn render-tenants [data]

  (om/root data tenants-view (. js/document (getElementById "my-app"))))





(defn get-tenants [token-id]
  (GET
   "/tenants"
   {:handler (fn [response]

               (if (:success response)
                 (do
                   (swap! app-state assoc :tenants (:tenants response))
                   (render-tenants app-state))
                 (do
                   (swap! app-state assoc :tenants (:tenants response))
                   (render-tenants app-state))

                 )
               )
    :error-handler error-handler
    :params {:url (:url @app-state) :token-id token-id}
    :response-format :json
    :keywords? true}))


(go
 (loop []
   (let [token (<! connect-channel)]
     (println token)
     (get-tenants token)
     (recur))

   )

 )

(comment (GET
          "/endpoints"
          {:handler (fn [response]
                      (if (:success response)
                        (swap! app-state assoc :enpoints (:endpoints response))
                        (swap! app-state assoc :endpoints (:endpoints response)) ;TODO                (js/alert "no tenants!\n" response)
                        )
                      )
           :error-handler error-handler
           :params {:url (:url @app-state) :username (:username @app-state) :password (:password @app-state) :tenantname (:name (first (:tenants @app-state)))}
           :response-format :json
           :keywords? true}))

(defn widget-form [data owner]
  (reify
    om/IRender
    (render [this]

      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a connection")
                (dom/input #js {:ref "url" :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true :value url} )
                (dom/input #js {:ref "username" :type "text" :className "form-control" :placeholder "User Name" :required true :value username} )
                (dom/input #js {:ref "password" :type "password" :className "form-control" :placeholder "Password" :required true :value password}  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(try-to-connect % data owner)} "Connect!")
                ))))
                                        ;(om/root app-state widget-form (. js/document (getElementById "my-app")))

(defn widget-form-tenant [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a tenant")
                (dom/input #js {:ref "url" :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true :value url} )
                (dom/input #js {:ref "username" :type "text" :className "form-control" :placeholder "User Name" :required true :value username} )
                (dom/input #js {:ref "password" :type "password" :className "form-control" :placeholder "Password" :required true :value password}  )
                (dom/input #js {:ref "tenant" :type "text" :className "form-control" :placeholder "Tenant name" :required true :value username}  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(try-to-connect % data owner)} "Connect!")
                ))))

(defn render-connect []
  (om/root (swap! app-state assoc :connection :base) widget-form (. js/document (getElementById "my-app"))))
(defn render-connect-tenant []
  (om/root (swap! app-state assoc :connection :tenant)  widget-form-tenant (. js/document (getElementById "my-app"))))

(render-connect)

(comment
  (om/root (swap! app-state assoc :text "holooo") widget (. js/document (getElementById "my-app")))

  (defn hello
    []
    (js/alert "hello"))

  (defn whoami
    []
    (.-userAgent js/navigator))
  )



(defn navbar [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/nav #js {:className "navbar navbar-default" :role "navigation"}
               (dom/div #js {:className "collapse navbar-collapse"}
                        (dom/ul #js {:className "nav navbar-nav"}

                                (dom/li #js {:className (when (= :base (:connection data)) "active") }
                                        (dom/a #js {:href "#" :onClick render-connect} "Try a connection"))
                                (dom/li #js {:className (when (= :tenant (:connection data)) "active")  }
                                        (dom/a #js {:href "#" :onClick render-connect-tenant} "Connect to a tennant"))))))))
(render-connect)
(om/root app-state navbar (. js/document (getElementById "my-nav")))
