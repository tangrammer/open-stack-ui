(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
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
   [cljs.core.async :refer [put! chan <! >! sliding-buffer]])
  )


(enable-console-print!)

(declare shared)
(defn dir [o]
  (.dir js/console o))

(defn menu [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:selection (chan)
       :next-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [selection (om/get-state owner :selection) flow (om/get-state owner :flow)]
        (go (loop []
              (let [selection-type (<! selection)]
                (put! flow [selection-type (om/get-state owner :next-chan)])
                (recur))))))
    om/IRenderState
    (render-state  [this {:keys [selection]}]
      (dom/div #js {:id "menu"  :style #js{:float "left" :margin-right "50px" :width "300px"}}
               (dom/ul #js {:className "nav nav-tabs nav-stacked"}
                       (dom/li #js {:ref "welcome" :className ""}
                               (dom/a #js {:href "#" :onClick #(do
                                        ;(change-cssclass owner "tenant" "active")
                                        ;(change-cssclass owner "base" "")
                                                                 (put! selection :welcome))} "Welcome to OS UI"))
                       (dom/li #js {:ref "connection" :className ""}
                               (dom/a #js {:href "#"
                                           :onClick #(do
                                        ;(change-cssclass owner "base" "active")
                                        ;(change-cssclass owner "tenant" "")
                                                       (put! selection :connection))
                                           } "Connect to a OS Instance"))
                       )


               ))))

(def content-chan (chan))


(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (println "INIT STATE content")
      {:flow  content-chan
       :stock :welcome
       :next-chan (chan (sliding-buffer 1))
       })
    om/IDidMount
    (did-mount [_  _]
      (println "DID MOUNT OK content")
      )
    om/IDidUpdate
    (did-update [_ _ _ _]
      (println (str "DID UPDATE OK content" (om/get-state owner :in-chan)))
      (put! (om/get-state owner :in-chan) [(om/get-state owner :flow) (om/get-state owner :next-chan)])                                        ;      (put!  "UPDATE OK *******************************")
      )
    om/IWillMount
    (will-mount [_]
      (println "WILL MOUNT OK content")
      (let [flow  (om/get-state owner :flow)]
        (go (loop []
              (let [[flow-state in-chan]  (<! flow)]

                (println (str "type::: "(keyword? flow-state)))
                                        ;                (om/transact! app :flow-state (fn [_] flow-state))
                (om/set-state! owner :stock flow-state)
                (recur))))))

    om/IRenderState
    (render-state [this state]
      (println "RENDER-STATE content")
      (let [flow-state  (:stock state)]
        (dom/div #js {:id "content" :style #js {  :width "100%" }}
                 (om/build menu app {:init-state state} )
                                        ;
                 (if (keyword? flow-state)
                   (condp = flow-state
                     :welcome (dom/h2 nil (str "Welcome!! " (:flow-state app)))
                     :connection (do
                                   (dom/h2 nil (str "connection!!! " (om/get-state owner :next-chan)))
                                   (om/build conns/connections app {:init-state {:in-chan (om/get-state owner :next-chan) :flow (om/get-state owner :flow)}} )
                                   )
                     :endpoints (om/build eps/epss app )
                     :tenants (om/build tenants/tenants app )
                     :service (do (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                           (dom/h2 nil (str "service call!: " (:model app)))

                                           (dom/button #js {:className "btn  btn-primary " :type "button"
                                                            :onClick #(put! (om/get-state owner :flow) [ :endpoints (om/get-state owner :in-chan)])} "endpoints again!")
                                           (dom/pre nil (dom/code nil (JSON/stringify (clj->js ((:model app) app)) nil 2)))))
                     (js/alert (str  "else" flow-state))
                     )
                                        ;(js/alert "ofu")
                   (flow-state app)
                   )

                 )))))

(defn container [app owner]
  (reify
    om/IRender
    (render [this ]
      (dom/div #js {:id "container" :style #js {:width "1200px" }}
                                        ;(dom/h2 nil "Container")


               (om/build content app {:init-state {:in-chan (:next-chan shared)}})
               ))))
(def app-state (atom {:title "the app tittle" :menu "the menu" :flow-state :welcome}))



(def shared {:next-chan (chan (sliding-buffer 1))})
(om/root app-state  container (. js/document (getElementById "my-app")))



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
  (to-out-channel in model))


(defn start [ in model]
 ;       (put! (om/get-state owner :in-chan) [(om/get-state owner :connection) (om/get-state owner :next-chan)])
  ;(go  (<! (:init-chan shared)[in ]))
  )

(defn go-to-sequence [section subsection]

  (go
    (>! content-chan [ section (:in-chan shared)])
    (let [[in-chan next] (<! (:in-chan shared))]
      (println "hereee++++++++++++++++++++++++++++++++++++++++")
      (>! in-chan subsection)
      (<! next)
      )))
(defn go-to-tenants-after-base-connection [section subsection tenants]
  (go (let [[in-chan next-chan] (<! (go-to-sequence section subsection))]
        (println "inside++++++++++++++++++++++++++++++++++++++++")
        (>! in-chan  {:token-id "eyyy" :tenants tenants})
        (<! next-chan))))

(comment (go-to-tenants-after-base-connection :connection :base mocks/tenants))


(defn go-to-tenants [in tenants]
  (to-out-channel in {:token-id "eyyy" :tenants tenants})
)




(comment
  (go
    (>! content-chan [ :welcome (:next-chan shared)])


    )

  (go

    (>! content-chan [ :connection (:next-chan shared)])
    (let [[ v n] (<! (:next-chan shared))]
      (>! v [ :connection n])
      (let [[ a {:keys [tenant]}] (<! n)]
        (>! a :tenant)
        (let [[c d ] (<! tenant)]
          (>! c {:endpoints mocks/eps :token-id "xxxxxxxx"})


          )

        )
      )
    )

  (go

    (>! content-chan [ :welcome (:next-chan shared)])
    (let [[ v n] (<! (:next-chan shared))]
      (>! v [ :connection n])
      (let [[ a {:keys [base]}] (<! n)]
        (>! a :base)
        (let [[c d ] (<! base)]
          (>! c {:token-id "xxxxxxxx" :tenants mocks/tenants})
          )

        )
      )
    )

  (let [connection-base (go-to-sequence :connection :base)]
    (to-out-channel connection-base {:token-id "xxxxxxxx" :tenants mocks/tenants})
    )
  (-> (go-to-sequence :connection :base)
      (inject {:token-id "xxxxxxxx" :tenants mocks/tenants}))
  (do

   (go-to-sequence :connection :tenant)
   (go-to-sequence :connection :base)
   (go-to-sequence :connection :tenant)
   (go-to-sequence :connection :base)
   (go-to-sequence :connection :tenant)
   ))
