(ns heroku.endpoints
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [heroku.nav :as nav]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [cljs.core.async :refer [put! chan <! sliding-buffer >!]])
  )

(defn service-call [channel service-id token-id publicURL url]

  (GET
   "/service-call"
   {:params {:token-id token-id :publicURL publicURL :path url}
    :handler (fn [response]
               (if (:success response)
                 (do
                   (put! channel {service-id (service-id response) :model service-id})
                   )
                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true})

  )


(defn eps [tenant owner]
  (reify
    om/IInitState
    (init-state [_]
      {})

    om/IRenderState
    (render-state [this  {:keys [own-chan token-id]}]

      (apply dom/li #js {:className "list-group-item" } (:name tenant)
             (map
              (fn [av]
                (dom/button #js {
                                 :onClick #(do
                                        ;(.dir js/console owner)
                                             (service-call own-chan
                                                           (:id av)
                                                           token-id
                                                           (:publicURL @tenant) (:url av))
                                             )
                                 :className "btn btn-primary btn-xs"} (:url av)) )

              ((keyword (:name tenant)) util/available-calls )

              )

             ))))

(defn epss [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:own-chan (chan)

       :next-chan (chan (sliding-buffer 1))})

    om/IWillMount
    (will-mount [_]




      (let [try-to-call (om/get-state owner :own-chan)
            flow (om/get-state owner :flow)]
        (go (loop []
              (>! (om/get-state owner :in-chan) [(om/get-state owner :own-chan) {:next (om/get-state owner :next-chan)}])
              (let [data-readed (<! try-to-call)]
                (om/update! app :model (:model data-readed))
                (om/update! app (:model data-readed) ((:model data-readed) data-readed))
                (condp = (:model data-readed)
                  :images (>! flow :images )
                  :flavors (>! flow :flavors )
                  :create-server (>! flow :create-server )
                  (>! flow :service ))
                (recur))))))
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "row"  :style #js {:float "left"  :width "800px"}}
               (dom/div #js {:className "col-md-6 col-md-offset-3"}
                        (dom/h2 #js {:style {:padding-left "100px"}} "endpoints list")

                        (dom/button #js {:className "btn  btn-primary " :type "button"
                                         :onClick
                                         #(let [images (chan)
                                                flavors (chan)
                                                networks (chan)

                                                ]
                                            (service-call images :images (:token-id @app) (get-in @app [:endpoints :compute :publicURL]) "/images")
                                            (service-call flavors :flavors (:token-id @app) (get-in @app [:endpoints :compute :publicURL]) "/flavors")
                                            (service-call networks :networks (:token-id @app) (get-in @app [:endpoints :network :publicURL]) "/v2.0/networks")


                                            (go
                                              (let [res {:create-server {:flavors (:flavors (<! flavors)) :networks (:networks (<! networks)) :images (:images (<! images))}
                                                         :model :create-server}]

                                                (>! (om/get-state owner :own-chan) res)
                                                ))




                                            )} "create-server!")

                        (apply dom/ul #js {:className "list-group"}
                               (om/build-all eps  (vals (:endpoints app)) {:init-state {:token-id (:token-id app)
                                                                                        :own-chan (om/get-state owner :own-chan)
                                                                                        } :state state})))))))


(comment {:in-chan (om/get-state owner :next-chan)
          :flow (:flow state)  })
