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



(enlive/deftemplate page-dev
  (io/resource "development.html")
  []
  [:body] (enlive/append
           (enlive/html [:script (browser-connected-repl-js)])))
(enlive/deftemplate page-prod
  (io/resource "production.html")
  []
  [:body] (enlive/append
           (enlive/html [:h1 "production"])))


(defroutes app
  (resources "/")
  (resources "/out")
  (GET "/dev" [] (page-dev))
  (GET "/prod" [] (page-prod))
  (ANY "/" req (page-prod)))

(defroutes app1

  (ANY "/connect" [url username password]
       (println "." url "." username "." password ".")
       (resp/response (tokens url username password)))
  (ANY "/tenants" [url token-id]
       (println url token-id)
       (resp/response (tenants url token-id)))
    (ANY "/endpoints" [url username password tenantname]
       (println url username password tenantname)
       (resp/response (endpoints url username password tenantname)))
    (ANY "/service-call" [token-id publicURL path]
         (println token-id publicURL path)
         (let [r (service-call token-id publicURL (reduce str "" (rest path)))]
           (println r)
           (resp/response r)))

)


(declare server)

(def handler
  (comphand/api (routes
                 app
                 (-> app1
                     (middleware/wrap-json-response  {:keywords? true})))))


(defn -main [port]
   (def server (run-jetty #'handler {:port (Integer. port) :join? false }))
)

(defn run []
  (-main 5000)
  )

(defn stop []
  (.stop server)
  )

(comment  "to start:
 * cljsbuild once to generate the js
 * brepl evaluate both lines and reload browser
 * on the brepl enter this line (js/alert 'here')"

          (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                               (cemerick.austin/repl-env)))
          (cemerick.austin.repls/cljs-repl repl-env)

         )
