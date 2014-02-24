(ns heroku.connections
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [heroku.mac :refer [t minimal alert wurivagnuc listen-component]])
  (:require
   [heroku.login :as login]
   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.endpoints :as eps]
   [heroku.tenants :as tenants]
   [heroku.endpoints :as endpoints]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [cljs.core.async :refer [put! chan <! sliding-buffer >! dropping-buffer] ])
  )

(defn get-value [owner ref]
  (let [input (om/get-node owner ref)]
    (.-value input)
    ))

(defn connect-tenants-list [channel url  username password token-id]
  (GET
   "/tenants"
   {:params {:url url :token-id token-id}
    :handler (fn [response]

               (if (:success response)
                 (do
                   (put! channel  {:token-id token-id :tenants (:tenants response) :url url :username username :password password}))
                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true}))

(defn connect-base [channel url  username password]
  (GET
   "/connect"
   {:params {:url url :username username :password password}
    :handler (fn [response]

               (if (:success response)
                 (connect-tenants-list channel url  username password (get-in response [:access :token :id]))

                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true}))

(defn base [data owner]
  (reify

    om/IInitState
    (init-state [_]
      (println "init base component")
      {:own-chan (chan)
       :next-chan (chan (sliding-buffer 1))})
    om/IWillMount
    (will-mount [_]

      (go (loop []
(>! (om/get-state owner :in-chan) [(om/get-state owner :own-chan) {:next (om/get-state owner :next-chan)}])
            (println "component 'base' published")
            (let [data-readed (<! (om/get-state owner :own-chan))]

              (om/update! data :tenants (:tenants data-readed))
              (om/update! data :token-id (:token-id data-readed))
;              (>! (om/get-state owner :flow) :tenants)
              (>! (om/get-state owner :flow)
                  (fn [app] (om/build tenants/tenants app
                                     {:init-state {:in-chan (om/get-state owner :next-chan) :flow (om/get-state owner :flow)
                                                    :url (:url data-readed) :username (:username data-readed) :password (:password data-readed)}} )))
              (recur))))
      )

    om/IRenderState
    (render-state [this {:keys [own-chan flow]}]
      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a connection")
                (dom/label nil "Local VM http://192.168.56.102:5000")
                (dom/label nil  "Public local VM http://85.136.130.89:5000")
                (dom/label nil (str "trystack:  " login/url))
                (dom/input #js {:ref "url" :defaultValue login/url :type "text" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )

                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  login/username))

                (dom/input #js {:ref "username" :defaultValue login/username :type "text" :className "form-control" :placeholder "User Name" :required true } )
                (dom/label nil (str "Public and local VM: password"))
                (dom/label nil (str "trystack:"  login/password))
                (dom/input #js {:ref "password" :defaultValue login/password :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button"
                                 :onClick #(connect-base own-chan
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
    om/IInitState
    (init-state [_]
      (println "init tenant component")

      (util/init-continuations-channels
       { :own-chan (chan)}
       :next-chan )
      )
    om/IWillMount
    (will-mount [_]
      (util/publish-mount-state owner :own-chan )
      (go (loop []
;            (>! (om/get-state owner :in-chan) [(om/get-state owner :own-chan) {:next (om/get-state owner :next-chan)}])
            (println "component 'tenant' published")
            (let [data-readed (<! (om/get-state owner :own-chan))]
              (om/update! data :token-id (:token-id data-readed))
              (om/update! data :endpoints (:endpoints data-readed))
              (>! (om/get-state owner :flow)
                  (fn [app]
                    (listen-component :next-chan owner eps/epss app {:init-state { :flow (om/get-state owner :flow)}})))

              (recur))))
      )


    om/IRenderState
    (render-state [this {:keys [own-chan ]}]
      (dom/form #js {:className "form-signin" :role "form" }
                (dom/h2 #js {:className "form-signin-heading"} "Connect to Tenant")
                (dom/label nil "Local VM http://192.168.56.11:5000")
                (dom/label nil  "Public local VM http://85.136.130.89:5000")
                (dom/label nil (str "trystack:  " login/url))

                (dom/input #js {:ref "url" :defaultValue login/url :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )
                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  login/username))
                (dom/input #js {:ref "username" :defaultValue login/username :type "text" :className "form-control" :placeholder "User Name" :required true } )

                (dom/label nil (str "Public and local VM: password"))
                (dom/label nil (str "trystack:"  ))
                (dom/input #js {:ref "password" :defaultValue login/password :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/label nil (str "Public and local VM: admin/demo"))
                (dom/label nil (str "trystack:"  login/username))

                (dom/input #js {:ref "tenant" :defaultValue login/username :type "text" :className "form-control" :placeholder "Tenant name" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(connect-tennant own-chan
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
      (println "init connections component")
      (util/init-continuations-channels
       { :own-chan (chan)
       :connection-type :base}
       :base :tenant )
)

    om/IWillMount
    (will-mount [this]
      (println "will mount connections")
      (println (om/get-state owner :in-chan))
      (util/publish-mount-state owner :own-chan )
      ;(om/set-state! owner :connection  (om/get-state owner :in-chan))
      (let [connection (om/get-state owner :own-chan)]
        (go (loop []


              (println "published connections component***************")
              (let [connection-type (<! connection)]

                (println (str "getting connection-type" connection-type))
                (om/set-state!  owner :connection-type connection-type)
;                (println (str "mas:::::::::::::::::::"(om/get-state owner :connection-type)))
 ;               (println (str "************************* setting value" connection-type))
                (recur))))))

    om/IRenderState
    (render-state [this state]

;      (println (str "******" (om/get-state owner :connection-type)))
;      (println (str "******" (:connection-type state)))

      (let [connection-type ( :connection-type state)]
        (dom/div #js {:id "connections" :style #js {:float "left"  :width "800px"}}

                                        ;(dom/h2 nil (str "Content DIV" connection-type))
                 (dom/h3 nil (str "CONNECTION AREA" connection-type))
                 (om/build nav/navbar app {:init-state {:connection (om/get-state owner :own-chan)}} )
                 (if (= connection-type :base)
                   (listen-component :base owner base app {:init-state { :flow (om/get-state owner :flow)}}  )
                   (listen-component :tenant owner tenant app {:init-state { :flow (om/get-state owner :flow)}}  )
                   #_(om/build tenant app {:init-state {:in-chan (:next-chan-tenant (om/get-state owner :nexts)) :flow (:flow state)  }
                                         } )
                   )
                 ))
      )))
