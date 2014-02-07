(ns heroku.tenants
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [heroku.nav :as nav]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]])
  )


(defn tenant [tenant owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js {:className "list-group-item" :style #js {:float "left"  :width "800px"}} (:name tenant)))))

(defn tenants [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js { :style #js {:float "left"  :width "800px"}}
               (dom/h2 nil "Tenants list")
               (apply dom/ul #js {:className "list-group"}
                      (om/build-all tenant (:tenants app)))))))
