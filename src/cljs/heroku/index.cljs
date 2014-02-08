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
   [cljs.core.async :refer [put! chan <!]])
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


(defn content [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:flow (chan)})

    om/IWillMount
    (will-mount [_]
      (let [flow (om/get-state owner :flow)]
        (go (loop []
              (let [flow-state (<! flow)]
                (om/transact! app :flow-state (fn [_] flow-state))
                (recur))))))

    om/IRenderState
    (render-state [this state]
     ; (println "reading" (:flow-state app))

      (let [flow-state (:flow-state app)]
        (dom/div #js {:id "content" :style #js {  :width "100%" }}
                 (om/build menu app {:init-state state} )
                                        ;
                 (condp = flow-state
                   :welcome (dom/h2 nil (str "Welcome!! " (:flow-state app)))
                   :connection (om/build conns/connections app {:init-state state} )
                   :endpoints (om/build eps/epss app {:init-state state})
                   :tenants (om/build tenants/tenants app )
                   :service (do (dom/div #js {:id "service" :style #js {  :width "100%" }}
                                         (dom/h2 nil (str "service call!: " (:model app)))
                                         (dom/button #js {:className "btn  btn-primary " :type "button"
                                                          :onClick #(put! (om/get-state owner :flow) :endpoints)} "endpoints again!")
                                         (dom/pre nil (dom/code nil (JSON/stringify (clj->js ((:model app) app)) nil 2)))))))))))

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
(om/build eps/epss app-state {:init-state state})

(defn testing [state]
  (swap! app-state assoc :flow-state state)
  )
