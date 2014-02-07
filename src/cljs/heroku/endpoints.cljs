(ns heroku.endpoints
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
(defn eps [tenant owner]
  (reify
    om/IRender
    (render [this]
      (apply dom/li #js {:className "list-group-item" } (:name tenant)
             (map
              (fn [av]
                (dom/button #js {:onClick
                                 :key "butt"
                                 #(js/alert "endpoint clicked!") :className "btn btn-primary btn-xs"} (:url av)) )
                                        ;(:available-calls @tenant)
              (:name tenant)
              )

             ))))

(defn epss [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "row"  :style #js {:float "left"  :width "800px"}}
               (dom/div #js {:className "col-md-6 col-md-offset-3"}
                        (dom/h2 #js {:style {:padding-left "100px"}} "endpoints list")
                        (apply dom/ul #js {:className "list-group"}
                               (om/build-all eps  (vals (:endpoints app)))))))))
