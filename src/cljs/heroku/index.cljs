(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)


(def app-state (atom {:text "Hello prototype!"}))
(def username "facebook1428467850")
(def password "3a34gc72")
(def url "http://8.21.28.222:5000")


(declare get-tenants show-endpoints endpoints-view)

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))
(om/root app-state widget (. js/document (getElementById "my-nav")))
(swap! app-state assoc :text "update5")


(swap! app-state assoc :text "Multiple roots!aaa")



(def connect-channel (chan))

(go
 (loop []
   (let [token (<! connect-channel)]
     (println token)
     (get-tenants token)
     (recur))
   )
 )

(def endpoints-channel (chan))

(go
 (loop []
   (let [readed (<! endpoints-channel)]
     (show-endpoints readed)
     (recur))
   )
 )
(defn show-endpoints [eps-and-token-id]

  (om/root (swap! app-state merge eps-and-token-id) endpoints-view (. js/document (getElementById "my-app")))
  )

(defn rerender-eps []
  (om/root app-state endpoints-view (. js/document (getElementById "my-app")))
  )


(defn try-to-call [token-id publicURL path]
  (println token-id publicURL path)

(GET
       "/service-call"
       {:params {:token-id token-id :publicURL publicURL :path path}
        :handler (fn [response]
                   (if (:success response)
                     (do
                       (swap! app-state assoc :flavors (:flavors response))
                                            (js/alert (:flavors response))
;                       (put! connect-channel  (get-in response [:access :token :id]))
                       )
                     (js/alert response)))
        :error-handler error-handler
        :response-format :json
        :keywords? true})

)

(defn endpoint-view [tenant owner]
  (reify
    om/IRender
    (render [this]
      (println tenant)
      (dom/li #js {:className "list-group-item" } (:name tenant)
              (doall (map
                 (fn [av] (dom/button #js {:onClick #(try-to-call (:token-id @app-state) (:publicURL @tenant) av) :className "btn btn-primary btn-xs"} av) ) (:available-calls tenant)
                 ))
              ))))

(defn endpoints-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "row"}
               (dom/div #js {:className "col-md-6 col-md-offset-3"}
                (dom/h2 #js {:style {:padding-left "100px"}} "endpoints list")
                (apply dom/ul #js {:className "list-group"}
                       (om/build-all endpoint-view  (vals (:endpoints app)))))))))


(defn try-to-connect [e data owner]
  (let [url (.-value (om/get-node owner "url"))
        password (.-value (om/get-node owner "password"))
        username (.-value (om/get-node owner "username"))]
    (if (= :base (:connection @app-state))
      (GET
       "/connect"
       {:params {:url url :password password :username username}
        :handler (fn [response]
                   (if (:success response)
                     (do
                       (swap! app-state assoc :url url)
                       (put! connect-channel  (get-in response [:access :token :id])))
                     (js/alert response)))
        :error-handler error-handler
        :response-format :json
        :keywords? true})
      (GET
       "/endpoints"
       {:params {:url url :password password :username username :tenantname (.-value (om/get-node owner "tenant"))  }
        :handler (fn [response]
                   (if (:success response)
                     (put! endpoints-channel {:token-id (get-in response [:access :token :id]) :endpoints (util/structured-endpoints response)})

                     (js/alert response)))
        :error-handler error-handler
        :response-format :json
        :keywords? true})
      )
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
   {
    :params {:url (:url @app-state) :token-id token-id}
    :handler (fn [response]

               (if (:success response)
                 (render-tenants (swap! app-state assoc :tenants (:tenants response)))
                 (js/alert "errorr")
                 )
               )
    :error-handler error-handler
    :response-format :json
    :keywords? true}))



(defn get-endpoints []
  (GET
   "/endpoints"
   {
    :params {:url (:url @app-state)
             :username (:username @app-state)
             :password (:password @app-state)
             :tenantname (:name (first (:tenants @app-state)))}
    :handler (fn [response]
               (if (:success response)
                 (swap! app-state assoc :enpoints (:endpoints response))
                 (swap! app-state assoc :endpoints (:endpoints response)) ;TODO                (js/alert "no tenants!\n" response)
                 )
               )
    :error-handler error-handler
    :response-format :json
    :keywords? true}))

(defn widget-form [data owner]
  (reify
    om/IRender
    (render [this]

      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a connection")
                (dom/h2 #js {:className "form-signin-heading" :visibility (if (nil? (:error data)) "hidden" "visible")} (:error data))
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

                (dom/h2 #js {:className "form-signin-heading"} ()
                        (dom/input #js {:ref "url" :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true :value url} ))
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
                                        (dom/a #js {:href "#" :onClick render-connect-tenant} "Connect to a Tennant"))))))))

(om/root app-state navbar (. js/document (getElementById "my-nav")))
