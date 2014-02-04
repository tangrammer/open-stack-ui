(ns heroku.util)


(def available-calls {:nova [{:url "/images" :id :images}
                             {:url "/flavors" :id :flavors}
                             {:url "/servers" :id :servers}
]
                      :quantum [{:url "/networks" :id :networks}
                                {:url "/subnets" :id :subnets}
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
