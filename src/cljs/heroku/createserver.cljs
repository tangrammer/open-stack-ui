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




(defn create! [channel url server-name token-id  flavor-href image-href network-id]
  (GET
   "/create-server"
   {:params {:url url :token-id token-id :server-name server-name :image-href image-href :flavor-href flavor-href :network-id network-id}
    :handler (fn [response]
               (if (:success response)
                 (do
                   (println response)
                   (put! channel [:server (:server response)])
                   )
                 (js/alert response)))
    :error-handler util/error-handler
    :response-format :json
    :keywords? true})
  )




(defn gg-value [owner ref]

  (let [v (om/get-node owner ref)]
    (.dir js/console (.-selectedIndex (.-options  v )))
    (.-selectedIndex (.-options  v )))
  )

(defn option [opt owner]
  (reify
    om/IRenderState
    (render-state [_ _]
      (.log js/console (clj->js opt))
      (dom/option #js {:className "list-group-item" :style #js {:float "left"  :width "800px" }} (:name opt)))))

(defn select [app owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [select-chan list-model select-name]}]
      (dom/div nil
               (dom/label nil (str "selecting " select-name))

             (dom/br nil "")

             (apply dom/select #js{:defaultValue 0 :ref select-name
                                   :onChange (fn [e]
                                               (let [selected-index (gg-value owner select-name)
                                                     ]
                                                 (.dir js/console @list-model)
                                                 (put! select-chan [ select-name (get  @list-model (dec selected-index))])))}
                    (dom/option #js{ :disabled true} "") ;:disabled true
                    (om/build-all option (om/get-state owner :list-model))))


      )))


(defn main-form [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:own-chan (chan)})

    om/IWillMount
    (will-mount [this]
      (let [connection (om/get-state owner :own-chan)]
        (go (loop []
              (let [[k v] (<! connection)]
                (if (= k :server)
                  (do
                    (om/update! app k v)
                    (>! (om/get-state owner :flow) :server-created)
                      )
                  (om/set-state! owner k v)
                  )
                (println (str "***************" k v))

                (recur))))))

    om/IRenderState
    (render-state [this state]
      (dom/div nil
               (dom/div nil

                   (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :images]) ) nil 2)))
               (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :flavors]) ) nil 2)))
               (dom/pre nil (dom/code nil (JSON/stringify (clj->js (get-in app [:create-server :networks]) ) nil 2)))


                        (dom/h3 nil (str "create server area" ))

                        (dom/form #js {:className "form-signin" :role "form" }


                                  (dom/input #js {:ref "server-name" :defaultValue "test-server" :type :text :className "form-control" :placeholder "Put your server name" :required true :autoFocus true } )




                                  (om/build select app {:init-state {:select-chan (om/get-state owner :own-chan) :select-name :image-select :list-model (get-in app [:create-server :images])}} )
                                  (om/build select app {:init-state {:select-chan (om/get-state owner :own-chan) :select-name :flavor-select :list-model (get-in app [:create-server :flavors])}} )
                                  (om/build select app {:init-state {:select-chan (om/get-state owner :own-chan) :select-name :network-select :list-model (get-in app [:create-server :networks])}} )
                                  (dom/br nil "")
                                  (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button"
                                                   :onClick #(let [sn (util/get-value owner "server-name")
                                                                   i (om/get-state owner :image-select)
                                                                   f (om/get-state owner :flavor-select)
                                                                   n (om/get-state owner :network-select)
       ]
                                                               (when (and i f n)
                                                                 (let [-i   (:href (first (:links i)))
                                                                       -f (:href (first (:links f)))
                                                                       -n (:id n)
                                                                       url (str (:publicURL (:compute (:endpoints @app))) "/servers")
                                                                       token-id (:token-id @app)]
                                                                   ;create![channel url server-name token-id  flavor-href image-href network-id]
                                                                   (create! (om/get-state owner :own-chan) url sn token-id -f -i -n )
                                                                   )))}
                                              "Create!")
                                  )


                        )
               )

      #_(let [connection-type ( :connection-type state)]
          (dom/div #js {:id "connections" :style #js {:float "left"  :width "800px"}}
                   ))
      )))
