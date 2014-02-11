(ns heroku.connections
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.tenants :as tenants]
   [heroku.endpoints :as endpoints]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]])
  )

(def username "facebook1428467850")
(def password "3a34gc72")
(def url "http://8.21.28.222:5000")


(defn get-value [owner ref]
  (let [input (om/get-node owner ref)]
    (.-value input)
    ))

(defn connect-tenants-list [channel url token-id]
  (GET
   "/tenants"
   {:params {:url url :token-id token-id}
    :handler (fn [response]
               (println response)
               (if (:success response)
                 (do
                                        ;(swap! app-state assoc :url url)
                   (put! channel  {:token-id token-id :tenants (:tenants response)}))
                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true}))

(defn connect-base [channel url  username password]
  (GET
   "/connect"
   {:params {:url url :username username :password password}
    :handler (fn [response]
               (println response)
               (if (:success response)
                 (connect-tenants-list channel url (get-in response [:access :token :id]))

                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true}))

(defn base [data owner]
  (reify


    om/IRenderState
    (render-state [this {:keys [try-to-connect flow]}]
      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a connection")
                (dom/label nil "Local VM http://192.168.56.102:5000")
                (dom/label nil  "Public local VM http://85.136.130.89:5000")
                (dom/label nil (str "trystack:  " url))
                (dom/input #js {:ref "url" :defaultValue url :type "text" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )

                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  username))

                (dom/input #js {:ref "username" :defaultValue username :type "text" :className "form-control" :placeholder "User Name" :required true } )
                (dom/label nil (str "Public and local VM: password"))
                (dom/label nil (str "trystack:"  password))
                (dom/input #js {:ref "password" :defaultValue password :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button"
                                 :onClick #(connect-base try-to-connect
                                                         (get-value owner "url")
                                                         (get-value owner "username")
                                                         (get-value owner "password") )} "Connect!")
                (dom/h1 nil " ")
                (dom/button #js {:className "btn  btn-inverse  btn-mini" :type "button"
                                 :onClick #(put! flow :welcome)} "Exit!")
                ))))
;(put! flow :welcome)
(defn connect-tennant [channel url username password tenant]
  (GET
   "/endpoints"
   {:params {:url url :password password :username username :tenantname tenant  }
    :handler (fn [response]
               (if (:success response)
                 (put! channel {:token-id (get-in response [:access :token :id]) :endpoints (util/structured-endpoints response)})

                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true})
  )

(defn tenant [data owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [try-to-connect]}]
      (dom/form #js {:className "form-signin" :role "form" }
                (dom/h2 #js {:className "form-signin-heading"} "Connect to Tenant")
                (dom/label nil "Local VM http://192.168.56.102:5000")
                (dom/label nil  "Public local VM http://85.136.130.89:5000")
                (dom/label nil (str "trystack:  " url))

                (dom/input #js {:ref "url" :defaultValue url :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )
                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  username))
                (dom/input #js {:ref "username" :defaultValue username :type "text" :className "form-control" :placeholder "User Name" :required true } )

                (dom/label nil (str "Public and local VM: password"))
                (dom/label nil (str "trystack:"  password))
                (dom/input #js {:ref "password" :defaultValue password :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  username))

                (dom/input #js {:ref "tenant" :defaultValue username :type "text" :className "form-control" :placeholder "Tenant name" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(connect-tennant try-to-connect
                                                                                                                         (get-value owner "url")
                                                                                                                         (get-value owner "username")
                                                                                                                         (get-value owner "password")
                                                                                                                         (get-value owner "tenant") )} "Connect!")
                ))))
(comment
  si recibo un channel)
(defn connections [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {
       :connection-type :base})

    om/IWillMount
    (will-mount [this]
      (om/set-state! owner :connection  (om/get-state owner :in-chan))
      (om/set-state! owner :hola "hola" )
      (let [connection (om/get-state owner :connection)]
        (go (loop []
              (let [connection-type (<! connection)]
                (om/set-state!  owner :connection-type connection-type)
                (recur))))))

    om/IRenderState
    (render-state [this state]
      (println "reading" (om/get-state owner :connection-type))
      (let [connection-type (om/get-state owner :connection-type)]
        (dom/div #js {:id "connections" :style #js {:float "left"  :width "800px"}}

                                        ;(dom/h2 nil (str "Content DIV" connection-type))
                 (dom/h3 nil (om/get-state owner :hola))
                 (om/build nav/navbar app {:init-state state} )
                 (if (= connection-type :base)
                   (om/build base app {:init-state (let [c (chan)
                                                         flow (om/get-state owner :flow)]

                                                     (go (loop []
                                                           (let [data-readed (<! c)]
                                                             (om/update! app merge data-readed)
                                        ;                                                             (put! flow :tenants)
                                                             (put! flow (fn [app ](om/build tenants/tenants app )))
                                                             (recur))))

                                                     (assoc state :try-to-connect c))} )
                   (om/build tenant app {:init-state (let [c (chan)
                                                           flow (om/get-state owner :flow)]

                                                       (go (loop []
                                                             (let [data-readed (<! c)]
                                                               (om/update! app merge data-readed)
                                                               (put! flow :endpoints)
                                                               (recur))))

                                                       (assoc state :try-to-connect c))} )
                   )
                 ))
      )))
