(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [heroku.mac :refer [t minimal]])
  (:require
   [heroku.mocks :as mocks]
   [heroku.util :as util]
   [heroku.nav :as nav]
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

(def shared-chan (chan (dropping-buffer 1) ))

(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:flow  content-chan
       :stock :welcome
       :next-chan-connections (chan (dropping-buffer 1))
       :next-chan-eps (chan (dropping-buffer 1))
       :next-chan-tenants (chan (dropping-buffer 1))
       })
    om/IDidUpdate
    (did-update [_ _ _ _]
      (println "CONTENT  updated")
;      sliding-buffer

      )
    om/IWillMount
    (will-mount [_]
      (println "WILL MOUNT OK content")

      (let [flow  (om/get-state owner :flow)]
        (go (loop []
              (>! (om/get-state owner :in-chan) [(om/get-state owner :flow) {:connections (om/get-state owner :next-chan-connections)
                                                                             :eps (om/get-state owner :next-chan-eps)
                                                                             :tenants (om/get-state owner :next-chan-tenants)}])
              (let [flow-state   (<! flow)]

                (println (str "type::: " flow-state))
                                        ;                (om/transact! app :flow-state (fn [_] flow-state))
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
                                           {:init-state {:in-chan (om/get-state owner :next-chan-connections) :flow (om/get-state owner :flow)}} )

                     :endpoints (om/build eps/epss app {:state {:in-chan (om/get-state owner :next-chan-eps) :flow (om/get-state owner :flow)}} )
                     :tenants (om/build tenants/tenants app {:state {:in-chan (om/get-state owner :next-chan-tenants) :flow (om/get-state owner :flow)}} )
                     :service (do
                                (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                           (dom/h2 nil (str "service call!: " (:model app)))

                                           (dom/button #js {:className "btn  btn-primary " :type "button"
                                                            :onClick #(put! (om/get-state owner :flow) [ :endpoints (om/get-state owner :in-chan)])} "endpoints again!")
                                           (dom/pre nil (dom/code nil JSON/stringify (clj->js ((:model app) app)) nil 2))))
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
                                        ;(dom/h2 nil "Container")


               (om/build content app {:init-state {:in-chan shared-chan}})
               ))))
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

        ))

  (go
    (>! content-chan :welcome)
    (-> (t connection-type-channel :connection :connections)
        (t :base :base)
        (t {:token-id "xxxxxxxx" :tenants mocks/tenants} :next)))
(println "\n\n\n")

  (go
    (>! content-chan :welcome)


    )

  (go
    (>! content-chan :welcome)

    (let [conns (go (let [[out {:keys [connections]}] (<! connection-type-channel)]
                   (>! out  :connection)
                   (<! connections)))
          tenant (go (let [[out {:keys [tenant]}] (<! conns)]
                   (>! out  :tenant)
                   (<! tenant)))
          x (go (let [[out _] (<! tenant)]
                  (>! out  {:endpoints mocks/eps :token-id "xxxxxxxx"})
                  (<! _)))]
      (<! x)
      )
    )

  (go
    (>! content-chan :welcome)
    (let [conns (go (let [[out {:keys [connections]}] (<! connection-type-channel)]
                   (>! out  :connection)
                   (<! connections)))
          base (go (let [[out {:keys [base]}] (<! conns)]
                   (>! out  :base)
                   (<! base)))
          x (go (let [[out _] (<! base)]
                  (>! out {:token-id "xxxxxxxx" :tenants mocks/tenants})
                  (<! _)))]
      (<! x)
      )
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
