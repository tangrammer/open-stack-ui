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

(defn service-call [channel token-id publicURL url]
  (println token-id publicURL url)
  (GET
   "/service-call"
   {:params {:token-id token-id :publicURL publicURL :path url}
    :handler (fn [response]
               (if (:success response)
                 (do
;                   (swap! app-state assoc (:id av) ((:id av) response))
                                        ; (put! channel  ((:id av) response))
                   (println response)
                   )
                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true})

  )


(defn eps [tenant owner]
  (reify

    om/IRenderState
    (render-state [this  {:keys [try-to-call app]}]
      (apply dom/li #js {:className "list-group-item" } (:name tenant)
             (map
              (fn [av]
                (dom/button #js {
                                 :onClick #(do
                                             ;(.dir js/console owner)
                                             (service-call (om/get-state owner :try-to-call) (:token-id @app) (:publicURL @tenant) (:url av))
                                             )
                                 :className "btn btn-primary btn-xs"} (:url av)) )

                                        ((keyword (:name tenant)) util/available-calls )

              )

             ))))

(defn epss [app owner]
  (reify
      om/IInitState
    (init-state [_]
      {:try-to-call (chan)})
      om/IWillMount
    (will-mount [_]
      (let [try-to-call (om/get-state owner :try-to-call)
            flow (om/get-state owner :flow)]
        (go (loop []
              (let [data-readed (<! try-to-call)]
                                        ;                (om/update! data merge data-readed)
                (println data-readed)
                (put! flow :service)
                (recur))))))
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "row"  :style #js {:float "left"  :width "800px"}}
               (dom/div #js {:className "col-md-6 col-md-offset-3"}
                        (dom/h2 #js {:style {:padding-left "100px"}} "endpoints list")
                        (apply dom/ul #js {:className "list-group"}
                               (om/build-all eps  (vals (:endpoints app)) {:init-state (assoc state :app app)})))))))
