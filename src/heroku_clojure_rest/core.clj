(ns heroku-clojure-rest.core
    (:use [open-stack-wrapper.core])
   (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]))

(def username "facebook1428467850")
(def password "3a34gc72")
(defroutes app
  (ANY "/" [] (resource :available-media-types ["application/json"]
                        :handle-ok  {:success true}))
  (ANY "/tokens" [] (resource :available-media-types ["application/json"]
                              :handle-ok (fn [ctx]  {:success true :token-id (get-in (tokens username password) [:access :token :id])})))
  (ANY  "/endpoints/:tenant" [tenant]  (resource :allowed-methods [:post :get]
                                          :available-media-types ["application/json"]
                                          :handle-ok (fn [_]  {:success true :endpoints (structured-endpoints (endpoints username password tenant))}))))



(def handler
  (-> app
      (wrap-params)))
(comment
 (run-jetty #'handler {:port 3000}))

(defn -main [port]
   (run-jetty #'handler {:port (Integer. port) :join? false})
)
