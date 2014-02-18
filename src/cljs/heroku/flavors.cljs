(ns heroku.flavors
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [heroku.nav :as nav]
   [ajax.core :refer [GET POST]]
   [heroku.endpoints :as eps]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [cljs.core.async :refer [put! chan <! >! sliding-buffer]])
  )

#_(defn connect-tennant [channel url username password tenant]
  (GET
   "/endpoints"
   {:params {:url url :password password :username username :tenantname tenant  }
    :handler (fn [response]
               (if (:success response)
                 (put! channel {:token-id (get-in response [:access :token :id]) :endpoints (util/structured-endpoints response)})

                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true})
  )

(defn flavor [flav owner]
  (reify
    om/IInitState
    (init-state [_]
      {})
    om/IRenderState
    (render-state [this {:keys [own-chan]}]
      (dom/li #js {:className "list-group-item" :style #js {:float "left"  :width "800px" }} (:href (first (:links flav)))
              (dom/button #js {
                               :onClick (fn []
                                          #_(connect-tennant own-chan "http://192.168.1.23:5000" "admin" "password" (:name @tenant))
                                          (js/alert "here")
                                          )

                               :className "btn btn-primary btn-xs"}  (:name flav)))

      )))

(defn flavors [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:own-chan (chan)
       :next-chan (chan (sliding-buffer 1))})

    om/IWillMount
    (will-mount [_]
      (let [select-chan (om/get-state owner :own-chan)]
        (go (loop []
              (>! (om/get-state owner :in-chan) [(om/get-state owner :own-chan) {:next (om/get-state owner :next-chan)}])
              (let [data-readed (<! select-chan)]

                (om/update! app :token-id (:token-id data-readed))
                (om/update! app :endpoints (:endpoints data-readed))

                (>! (om/get-state owner :flow)
                    (fn [app] (om/build eps/epss app {:init-state {:in-chan (om/get-state owner :next-chan) :flow (om/get-state owner :flow)}} )))
                (recur))))))

    om/IRenderState
    (render-state [this state]
      (dom/div #js { :style #js {:float "left"  :width "800px"}}
               (dom/h2 nil "flavors list")
               (apply dom/ul #js {:className "list-group"}
                      (om/build-all flavor (:flavors app) {:init-state state}))))))
