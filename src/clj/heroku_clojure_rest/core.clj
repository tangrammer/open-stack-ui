(ns heroku-clojure-rest.core
    (:use [open-stack-wrapper.core])
   (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
             [net.cgrand.enlive-html :as enlive]
             [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]
            [compojure.route :refer (resources)]
            [clojure.java.io :as io]))

(def username "facebook1428467850")
(def password "3a34gc72")


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
(defroutes app
  (resources "/")
  (resources "/out")


  (ANY "/" req (page))
  (ANY "/tokens" [] (resource :available-media-types ["application/json"]
                              :handle-ok (fn [ctx]  {:success true :token-id (get-in (tokens username password) [:access :token :id])})))
  (ANY  "/endpoints/:tenant" [tenant]  (resource :allowed-methods [:post :get]
                                          :available-media-types ["application/json"]
                                          :handle-ok (fn [_]  {:success true :endpoints (structured-endpoints (endpoints username password tenant))}))))



(def handler
  (-> app
      (wrap-params)))
(comment
 (run-jetty #'handler {:port 5000}))

(defn -main [port]
   (run-jetty #'handler {:port (Integer. port) :join? false})
)

(defn run []
  (-main 5000)
  )
