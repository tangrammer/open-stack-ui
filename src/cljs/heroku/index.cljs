(ns heroku.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
       [goog.net.XhrIo :as xhr]
       [om.core :as om :include-macros true]
       [om.dom :as dom :include-macros true]
       [clojure.browser.repl]
       [cljs.core.async :refer [put! chan <!]]))

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))


(defn GET [url]
  (xhr/send url
            (fn [event]
              (let [res (-> event .-target .getResponseText)]
                (js/alert res)
                (om/root {:text res} widget (. js/document (getElementById "my-app")))
                ))))



(om/root {:text "Hello prototype!"} widget (. js/document (getElementById "my-app")))


(defn hello
  []
  (js/alert "hello"))

(defn whoami
  []
  (.-userAgent js/navigator))
