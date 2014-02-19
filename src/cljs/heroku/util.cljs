(ns heroku.util
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [heroku.mac :refer [t minimal]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [put! chan <! >! sliding-buffer dropping-buffer close!]])
    )

(defn get-value [owner ref]
  (let [input (om/get-node owner ref)]
    (.-value input)
    ))
(defn publish [suscriber own nexts ]
  (println (str "suscriber****************** " nexts))
  (let [cont (chan)]
    (go
;      (>! suscriber [own nexts])
      (loop []
        (if-let [in-own-value (<! own)]
          (do
            (>! suscriber [own nexts])
            (>! cont in-own-value)
            (recur))
          (close! cont))))
    cont))

(def available-calls {:nova [{:url "/images" :id :images}
                             {:url "/flavors" :id :flavors}
                             {:url "/servers" :id :servers}
]
                      :neutron [{:url "/v2.0/networks" :id :networks}
                                {:url "/v2.0/subnets" :id :subnets}
]})

(defn structured-endpoints [data]
  "in this development state we take the first endpoint available on each service"
  (let [services (get-in data [:access :serviceCatalog])]
    (reduce
     (fn [c service]
       (let [first-endpoint (first (:endpoints service))
             n (:name service)

             ]
         (assoc c (keyword (:type  service))
                {:name n
                 :available-calls (get available-calls (keyword n) [])
            :id (:id first-endpoint)
            :publicURL (:publicURL first-endpoint)})))
     {}
     services)))

(defn handler [response]
  (.log js/console (str response))
  )

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(comment
  (om/root (swap! app-state assoc :text "holooo") widget (. js/document (getElementById "my-app")))

  (defn hello
    []
    (js/alert "hello"))

  (defn whoami
    []
    (.-userAgent js/navigator))
  )
