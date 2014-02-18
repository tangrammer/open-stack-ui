(ns heroku.nav
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [cljs.core.async :refer [put! chan <!]])
  )


(defn change-cssclass [owner ref new-value]
  (set!  (.-className (om/get-node owner ref)) new-value)
  )


(defn navbar [data owner]
  (reify
    om/IRender
    (render  [this]
      (let [connection (om/get-state owner :connection)]
       (dom/nav #js {:className "navbar navbar-default" :role "navigation"}
                (dom/div #js {:className "collapse navbar-collapse"}
                         (dom/ul #js {:className "nav navbar-nav"}
                                 (dom/li #js {:ref "base" :className "active"}
                                         (dom/a #js {:href "#"
                                                     :onClick #(do
                                                                 (change-cssclass owner "base" "active")
                                                                 (change-cssclass owner "tenant" "")
                                                                 (put! connection :base))
                                                     } "Try a connection"))
                                 (dom/li #js {:ref "tenant" :className ""}
                                         (dom/a #js {:href "#" :onClick #(do
                                                                           (change-cssclass owner "tenant" "active")
                                                                           (change-cssclass owner "base" "")
                                                                           (put! connection :tenant))} "Connect to a Tenant")))))))))
