(ns heroku.createserver

  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.login :as login]
   [heroku.util :as util]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [put! chan <! sliding-buffer >! dropping-buffer] ])

  )

(defn g-value [owner ref]
  (let [input (om/get-node owner ref)]
    (.-value (.-selectedIndex input))
    ))


(defn main-form [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:own-chan (chan)
       })

    om/IWillMount
    (will-mount [this]
      (let [connection (om/get-state owner :own-chan)]
        (go (loop []
              (let [connection-type (<! connection)]
                (recur))))))

    om/IRenderState
    (render-state [this state]
      (dom/div nil
               (dom/h3 nil (str "create server area" ))
               (apply dom/select #js{:ref "juan"  :onChange
                                     (fn [e] (let [v (om/get-node owner "juan")]
                                              (.dir js/console (.-selectedIndex (.-options  v )))))}
                      (map #(dom/option #js{:value (:id %)} (:name %) )  (get-in app [:create-server :images]) ))
               (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :images]) ) nil 2)))
               (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :flavors]) ) nil 2)))
               (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :networks]) ) nil 2))))

      #_(let [connection-type ( :connection-type state)]
          (dom/div #js {:id "connections" :style #js {:float "left"  :width "800px"}}
                   ))
      )))
