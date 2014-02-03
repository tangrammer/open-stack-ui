(ns heroku-clojure-rest.core
    (:use [open-stack-wrapper.core])
   (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
             [net.cgrand.enlive-html :as enlive]

             [ring.middleware.params :refer [wrap-params]]
             [ring.util.response :as resp]
             [ring.adapter.jetty :refer [run-jetty]]
             [ring.middleware.json :as middleware]
             [compojure.core :refer [defroutes ANY POST GET routes]]
             [compojure.handler :as comphand]
            [compojure.route :refer (resources)]
            [clojure.java.io :as io]))

(def username "facebook1428467850")
(def password "3a34gc72")
(def url "http://8.21.28.222:5000")

(comment  "to start:
 * cljsbuild once to generate the js
 * brepl evaluate both lines and reload browser
 * on the brepl enter this line (js/alert 'here')"

          (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                               (cemerick.austin/repl-env)))
          (cemerick.austin.repls/cljs-repl repl-env)

         )

(enlive/deftemplate page
  (io/resource
   ;"production.html"
   "development.html"
   )
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))

(def posts (ref []))


(defroutes app
  (resources "/")
  (resources "/out")

  (GET "/dev" [] (page))

  (ANY "/" req (page))


 )

(defroutes app1

  #_(comment
           (ANY "/tokens" [] (resource :available-media-types ["application/json"]
                                       :handle-ok (fn [ctx]  {:success true :token-id (get-in (tokens url username password) [:access :token :id])})))
           (ANY  "/endpoints/:tenant" [tenant]  (resource :allowed-methods [:post :get]
                                                          :available-media-types ["application/json"]
                                                          :handle-ok (fn [_]  {:success true :endpoints (structured-endpoints (endpoints url username password tenant))}))))

  (ANY "/connect" [url username password]
       (println url username password)
       (resp/response {:success true :token-id (get-in (tokens url username password) [:access :token :id])}))
  (ANY "/tenants" [url token-id]
       (println url token-id)
       (resp/response (tenants url token-id)))
    (ANY "/endpoints" [url username password tenantname]
       (println url username password tenantname)
       (resp/response {:success true :endpoints (structured-endpoints (endpoints url username password tenantname))}))
)

(def admin-routes
  (-> app1
      (middleware/wrap-json-response  {:keywords? true})
))

(declare server)

(def handler
  (comphand/api (routes
                 app
                 admin-routes)
))
(comment
 (run-jetty #'handler {:port 5000}))

(defn -main [port]
   (def server (run-jetty #'handler {:port (Integer. port) :join? false }))
)

(defn run []
  (-main 5000)
  )

(defn stop []
  (.stop server)
  )
