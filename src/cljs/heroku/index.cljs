(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [heroku.mac :refer [t minimal]])
  (:require
   [heroku.mocks :as mocks]
   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.images :as imgs]
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


(defn dir [o]
  (.dir js/console o))

(defn menu [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:id "menu"  :style #js{:float "left" :margin-right "50px" :width "300px"}}
               (dom/ul #js {:className "nav nav-tabs nav-stacked"}
                       (dom/li #js {:ref "welcome" :className ""}
                               (dom/a #js {:href "#" :onClick #(do
                                        ;(change-cssclass owner "tenant" "active")
                                        ;(change-cssclass owner "base" "")
                                                                 (put! (om/get-state owner :flow) :welcome))} "Welcome to OS UI"))
                       (dom/li #js {:ref "connection" :className ""}
                               (dom/a #js {:href "#"
                                           :onClick #(do
                                        ;(change-cssclass owner "base" "active")
                                        ;(change-cssclass owner "tenant" "")
                                                       (put! (om/get-state owner :flow) :connection))
                                           } "Connect to a OS Instance"))
                       )


               ))))

(def content-chan (chan ))

(def shared-chan (chan (sliding-buffer 1) ))


(defn publish [suscriber own nexts ]
  (println (str "suscriber****************** " nexts))
  (let [cont (chan)]
    (go
;      (>! suscriber [own nexts])
      (loop []
        (if-let [in-own-value (<! own)]
          (do
            (>! suscriber [own nexts])
            (>! cont in-own-value)
            (recur))
          (close! cont))))
    cont))


(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (println (str "init content component" ))
      {:flow  content-chan
       :stock :welcome
       :nexts {:connections (chan (sliding-buffer 1))
               :eps (chan (sliding-buffer 1))
               :tenants (chan (sliding-buffer 1))
               :services (chan (sliding-buffer 1))
               :images (chan (sliding-buffer 1))
               :flavors (chan (sliding-buffer 1))}
       })
    om/IWillMount
    (will-mount [_]
      (om/set-state! owner  :flow (publish
                                   (om/get-state owner  :in-chan)
                                   (om/get-state owner :flow)
                                   (om/get-state owner :nexts)))
      (println "content component published")

      (let [flow  (om/get-state owner :flow)]
        (go (loop []
              (let [flow-state   (<! flow)]
                (println (str "content type::: " flow-state))
                (om/update! app :flow-state flow-state)
                (om/set-state! owner :stock flow-state)
                (recur))))))
    om/IRenderState
    (render-state [this state]

      (let [flow-state  (:stock state)]
        (dom/div #js {:id "content" :style #js {  :width "100%" }}
                 (om/build menu app {:state state} )
                                        ;
                 (if (keyword? flow-state)
                   (condp = flow-state
                     :welcome (dom/h2 nil (str "Welcome!! " (:flow-state app)))
                     :connection (om/build conns/connections app
                                           {:init-state {:in-chan (:connections (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )

                     :endpoints (om/build eps/epss app {:state {:in-chan (:eps (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
                                        ;changed to use function                     :tenants (om/build tenants/tenants app {:state {:in-chan (om/get-state owner :next-chan-tenants) :flow (om/get-state owner :flow)}} )
                     :images (om/build imgs/images app {:state {:in-chan (:images(om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
                     :flavors (om/build flavs/flavors app {:state {:in-chan (:flavors (om/get-state owner :nexts)) :flow (om/get-state owner :flow)}} )
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

(def app-state (atom {:title "the app tittle" :menu "the menu" :flow-state :welcome}))

(om/root container app-state {:target (. js/document (getElementById "my-app"))})

;;;;;;;;;;;;;;;;; TESTING ;;;;;;;;;;;;;;
(defn testing [state]
  (swap! app-state assoc :flow-state state)
  )


(defn to-out-channel [in model]
  (go (let [[out next] (<! in)]
        (>! out  model)
        (<! next)))
  )

(defn inject [in model]
  (go (let [[out next] (<! in)]
        (>! out  model)
        (<! next)))
  )

(def connection-type-channel shared-chan)
(defn result-of-inject [value channel]
  (inject channel value))


(comment

  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :tenant :tenant)
        (t {:endpoints mocks/eps :token-id "xxxxxxxx"} :next)
      (close!)

        ))

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
(println "\n\n\n\n\n\n\n\n\n")
