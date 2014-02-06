(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [heroku.nav :as nav]
   [heroku.connections :as conns]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]])
  )

(enable-console-print!)

(defn dir [o]
      (.dir js/console o)
  )

(def app-state (atom {:title "the app tittle" :nav "the nav"}))


(defn menu [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:id "menu"  :style #js{:float "left" :backgroundColor "orange" :color "white"}}
               (dom/h3 nil (:nav app))
        )))
  )


(defn content [app owner]
  (reify

    om/IWillMount
    (will-mount [_]
      (let [connection (om/get-state owner :connection)]
        (go (loop []
              (let [connection-type (<! connection)]
                (println "listened new connection type: " connection-type)
                (om/set-state! owner :connection-type connection-type)
                (println "setted: "(om/get-state owner :connection-type ))
                (recur))))))

    om/IRenderState
    (render-state [this {:keys [connection-type]}]


      (dom/div #js {:id "content" :style #js {:float "left"  :width "800px"}}

;               (dom/h2 nil "Content DIV")
 ;              (dom/h3 nil (:title @app))
               (println "aaaaa" connection-type)
               (if (= connection-type :base)
                 (om/build conns/base app )
                 (om/build conns/tenant app )
                 )
               )
)))


(defn container [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:connection (chan)
       :connection-type :base})
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:id "container" :style #js {:width "1000px" :backgroundColor "white"}}
               (dom/h2 nil "Container")
               (om/build nav/navbar app {:init-state state})
               (om/build menu app )
              (om/build content app {:init-state state})
               ))))

(om/root app-state container (. js/document (getElementById "my-app")))
