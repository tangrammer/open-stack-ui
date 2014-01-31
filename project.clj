(defproject com.enterpriseweb/heroku-clojure-rest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [om "0.3.0"]
                 [com.enterpriseweb/open-stack-wrapper "0.1.0"]
                 [liberator "0.10.0"]
                 [compojure "1.1.3"]
		 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [enlive "1.1.5"]

                 ]
  :plugins [[com.cemerick/austin "0.1.3"]
                             [lein-cljsbuild "1.0.1"]]
  :profiles {:dev {:repl-options {:init-ns heroku-clojure-rest.core :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :cljsbuild {:builds [{:id "dev"
                                         :source-paths ["src/cljs"]
                                         :compiler {
                                                    :output-to "target/classes/public/app.js"
                                                    :output-dir "target/classes/public/out"
                                                    :optimizations :none
                                                    :source-map true}}
                                        {:id "release"
                                         :source-paths ["src/cljs"]
                                         :compiler {
                                                    :output-to "resources/app.js"


                                                    :optimizations :advanced
                                                    :pretty-print false
                                                    :preamble ["react/react.min.js"]
                                                    :externs ["react/externs/react.js"]}}]}}}
  :uberjar-name "heroku-clojure-rest-standalone.jar"
  :min-lein-version "2.0.0"

  )
