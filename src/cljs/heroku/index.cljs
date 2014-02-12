(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require

   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.endpoints :as eps]
   [heroku.tenants :as tenants]
   [heroku.connections :as conns]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <! >!]])
  )

(enable-console-print!)

(defn dir [o]
  (.dir js/console o))

(defn menu [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:selection (chan)})
    om/IWillMount
    (will-mount [_]
      (let [selection (om/get-state owner :selection) flow (om/get-state owner :flow)]
        (go (loop []
              (let [selection-type (<! selection)]
                (put! flow selection-type)
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
(def content-in-chan (chan))

(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (println "INIT STATE content")
      {:flow  content-chan
       :stock :welcome
       :in-chan content-in-chan})
om/IDidMount
    (did-mount [_  _]
      (println "DID MOUNT OK content")
      )
    om/IDidUpdate
    (did-update [_ _ _ _]
      (println "DID UPDATE OK content")
;      (put!  "UPDATE OK *******************************")
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
                     :connection (om/build conns/connections app {:init-state state} )
                     :endpoints (om/build eps/epss app {:init-state state})
                     :tenants (om/build tenants/tenants app )
                     :service (do (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                           (dom/h2 nil (str "service call!: " (:model app)))
                                           (dom/button #js {:className "btn  btn-primary " :type "button"
                                                            :onClick #(put! (om/get-state owner :flow) :endpoints)} "endpoints again!")
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
    (render [this]
      (dom/div #js {:id "container" :style #js {:width "1200px" }}
                                        ;(dom/h2 nil "Container")


               (om/build content app )
               ))))
(def app-state (atom {:title "the app tittle" :menu "the menu" :flow-state :welcome}))


  (om/root app-state container (. js/document (getElementById "my-app")))

(defn testing [state]
  (swap! app-state assoc :flow-state state)
  )


(defn go-to-sequence [section subsection]

  (go

    (>! content-chan [ section content-in-chan])
    (let [[in-chan next] (<! content-in-chan )]
      (println "hereee++++++++++++++++++++++++++++++++++++++++")
      (>! in-chan subsection)))
  )

(do

  (go-to-sequence :connection :tenant)
   (go-to-sequence :connection :base)
   (go-to-sequence :connection :tenant)
   (go-to-sequence :connection :base)
   (go-to-sequence :connection :tenant)
   )
