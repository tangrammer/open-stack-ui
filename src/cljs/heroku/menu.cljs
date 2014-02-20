(ns heroku.menu
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [put! chan <!]])
  )

(defn menu [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:id "menu"  :style #js{:float "left" :margin-right "50px" :width "300px" :height "500px"}}
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
