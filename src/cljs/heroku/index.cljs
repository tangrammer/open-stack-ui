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
   [cljs.core.async :refer [put! chan <! >! sliding-buffer dropping-buffer]])
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
 (>! (om/get-state owner :in-chan) [(om/get-state owner :flow) (om/get-state owner :next-chan-connections)])
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
                                           {:state {:in-chan (om/get-state owner :next-chan-connections) :flow (om/get-state owner :flow)}} )

                     :endpoints (om/build eps/epss app )
                     :tenants (om/build tenants/tenants app )
                     :service (do
                                (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                           (dom/h2 nil (str "service call!: " (:model app)))

                                           (dom/button #js {:className "btn  btn-primary " :type "button"
                                                            :onClick #(put! (om/get-state owner :flow) [ :endpoints (om/get-state owner :in-chan)])} "endpoints again!")
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
                                        ;(dom/h2 nil "Container")


               (om/build content app {:init-state {:in-chan shared-chan}})
               ))))
(def app-state (atom {:title "the app tittle" :menu "the menu" :flow-state :welcome}))




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
    (>! content-chan section )
    (let [[in-chan next] (<! shared-chan)]

      (>! in-chan subsection)
      (<! next)
      )))
(defn go-to-tenants-after-base-connection [section subsection tenants]
  (go (let [[in-chan next-chan] (<! (go-to-sequence section subsection))]

        (>! in-chan  {:token-id "eyyy" :tenants tenants})
        (<! next-chan))))

(comment (go-to-tenants-after-base-connection :connection :base mocks/tenants))


(defn go-to-tenants [in tenants]
  (to-out-channel in {:token-id "eyyy" :tenants tenants})
)





(comment
  (go
    (>! content-chan :connection)
;    (>! content-chan :welcome)
    )

  (go
    (>! content-chan  :connection)
    (let [[ a {:keys [tenant]}] (<! shared-chan)]
      (>! a :tenant)
      (let [[c d ] (<! tenant)]
        (>! c {:endpoints mocks/eps :token-id "xxxxxxxx"})
        )

      )

    )


  (go
    (println "init ")
    (>! content-chan :connection)
    (println "exit 0 ")
      (let [[ v n] (<!  shared-chan)]
        (println "exit 1")
        (let [[ a {:keys [base]}] (<! n)]
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


  (go

    (>! content-chan :connection)


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
