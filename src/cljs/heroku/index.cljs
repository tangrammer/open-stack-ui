(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [heroku.mac :refer [t minimal alert wurivagnuc listen-component]])
  (:require
   [heroku.mocks :as mocks]
   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.menu :as men]
   [heroku.images :as imgs]
   [heroku.createserver :as create-server]
   [heroku.flavors :as flavs]
   [heroku.endpoints :as eps]
   [heroku.tenants :as tenants]
   [heroku.connections :as conns]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <! >! sliding-buffer dropping-buffer close!]])
  )

(enable-console-print!)

(def content-chan (chan ))

(def shared-chan (chan (sliding-buffer 1) ))

(def app-state (atom {:title "the app tittle" :menu "the menu" :flow-state :welcome}))

(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (println (str "init content component" ))
      (util/init-continuations-channels
       {:flow content-chan :stock :welcome }
       :connections :eps :create-server :tenants :services :images :flavors)
      )
    om/IWillMount
    (will-mount [_]
      (util/publish-mount-state owner :flow )


      (println "content component published")

      (let [flow  (om/get-state owner :flow)]
        (go (loop []
              (let [flow-state   (<! flow)]
                                        ;                (println (str "content type::: " flow-state))
                (om/update! app :flow-state flow-state)
                (om/set-state! owner :stock flow-state)
                (recur))))))
    om/IRenderState
    (render-state [this state]

      (let [flow-state  (:stock state)]
        (dom/div #js {:id "content" :style #js {  :width "100%" }}
                 (om/build men/menu app {:state state} )
                                        ;
                 (if (keyword? flow-state)
                   (condp = flow-state
                     :welcome (dom/h2 nil (str "Welcome!! " (:flow-state app)))
                     :connection (listen-component :connections owner
                                                   conns/connections app
                                                   {:init-state { :flow (om/get-state owner :flow)}}  )
                     #_(om/build conns/connections app
                                           {:init-state {:in-chan (:connections (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )

                     :endpoints (om/build eps/epss app {:state {:in-chan (:eps (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
                                        ;changed to use function                     :tenants (om/build tenants/tenants app {:state {:in-chan (om/get-state owner :next-chan-tenants) :flow (om/get-state owner :flow)}} )
                     :images (om/build imgs/images app {:state {:in-chan (:images(om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
                     :flavors (om/build flavs/flavors app {:state {:in-chan (:flavors (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
                     :create-server
                     (listen-component :create-server owner
                                                   create-server/main-form app
                                                   {:init-state { :flow (om/get-state owner :flow)}}  )


                     :server-created (do
                                       (dom/div nil
                                                (dom/h2 nil "server created ok!")
                                                (dom/pre nil (dom/code nil (JSON/stringify (clj->js (:server  app)) nil 2)))))
                     :service (do
                                (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                         (dom/h2 nil (str "service call!: " (:model app)))

                                         (dom/button #js {:className "btn  btn-primary " :type "button"
                                                          :onClick #(put! (om/get-state owner :flow)  :endpoints )} "endpoints again!")
                                         (dom/pre nil (dom/code nil (JSON/stringify (clj->js ((:model app) app)) nil 2)))))
                     (js/alert (str  "else" flow-state))
                     )
                                        ;(js/alert "ofu")
                   (flow-state app) ;special case
                   )

                 ))))
  )

(defn container [app owner]
  (reify
    om/IRender
    (render [this ]
      (dom/div #js {:id "container" :style #js {:width "1200px" }}
               (om/build content app {:init-state {:in-chan shared-chan}})))))

(om/root container app-state {:target (. js/document (getElementById "my-app"))})





(println "\n\n\n\n\n\n\n\n\n")
;;;;;;;;;;;;;;;;; TESTING ;;;;;;;;;;;;;;
(def connection-type-channel shared-chan)

(comment

  (go (>! content-chan :welcome))
  (go
    (>! content-chan :welcome)
    (-> (wurivagnuc connection-type-channel :connection :connections)
        (wurivagnuc :tenant :tenant)
        (wurivagnuc {:endpoints mocks/eps :token-id "xxxxxxxx"} :next-chan)

        (wurivagnuc {:create-server mocks/create-server :model :create-server} :next-chan)
        (wurivagnuc [:server mocks/server-created]  :next-chan)
        (close!)))
  (go
    (>! content-chan :welcome)
    (-> (wurivagnuc connection-type-channel :connection :connections)
        (wurivagnuc :base :base)
        (wurivagnuc {:token-id "xxxxxxxx" :tenants mocks/tenants} :next)
        (close!)))


  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :tenant :tenant)
        (t {:endpoints mocks/eps :token-id "xxxxxxxx"} :next)
        (t {:create-server mocks/create-server :model :create-server} :next)
        (close!)
        ))

  "once you have in memmory all these data ....."
  (let [real-eps (:endpoints @app-state)
        real-token (:token-id @app-state)
        flavors (:flavors @app-state)
        images (:images @app-state)
        networks (:networks @app-state)
        create-server {:create-server (:create-server @app-state) :model :create-server}
        ]
   (go
     (>! content-chan :welcome)
     (-> (t connection-type-channel :connection :connections)
         (t :tenant :tenant)
         (t {:endpoints real-eps :token-id real-token} :next)
         (t create-server :next)
         (close!)
         )))

  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :tenant :tenant)
        (t {:endpoints mocks/eps :token-id "xxxxxxxx"} :next)
        (t {:flavors mocks/flavors :model :flavors} :next)
        (close!)
        ))

  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :tenant :tenant)
        (t {:endpoints mocks/eps :token-id "xxxxxxxx"} :next)
        (t {:images mocks/images :model :images } :next)
        (close!)
        ))

  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :base :base)
        (t {:token-id "xxxxxxxx" :tenants mocks/tenants} :next)
        (t {:endpoints mocks/eps :token-id "xxxxxxxx"} :next)
        (t {:images mocks/images :model :images } :next)
        (close!)
      ))
  (println "\n\n\n")

  (go
    (>! content-chan :welcome)


    )



  (go
    (println "init ")
    (>! content-chan :connection)
    (println "exit 0 ")
    (let [[ v {:keys [ connections]} ] (<!  shared-chan)]
      (println "exit 1")
      (let [[ a {:keys [tenant]}] (<! connections)]
        (println "exit 2")
        (>! a :tenant)
        (println "exit 3")
        (let [[c d ] (<! tenant)]
          (println "exit 4")
          (>! c {:endpoints mocks/eps :token-id "xxxxxxxx"})
          (println "exit 5")
          ))))

  (go
    (println "init ")
    (>! content-chan :connection)
    (println "exit 0 ")
    (let [[ v {:keys [ connections]} ] (<!  shared-chan)]
      (println "exit 1")
      (let [[ a {:keys [base]}] (<! connections)]
        (println "exit 2")
        (>! a :base)
        (println "exit 3")
        (let [[c d ] (<! base)]
          (println "exit 4")
          (>! c {:token-id "xxxxxxxx" :tenants mocks/tenants})
          (println "exit 5")
          )
        )
      )
    )
  (println "\n")
  (put! content-chan :connection)
  (put! content-chan :welcome)


  )
