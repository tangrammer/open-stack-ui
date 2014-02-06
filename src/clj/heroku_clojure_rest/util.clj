(ns heroku-clojure-rest.util
  (:require [open-stack-wrapper.core :as cc :refer [structured-endpoints]]))

(defn str-endpoints [data]
  (cc/structured-endpoints data))
