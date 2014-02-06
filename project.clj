(defproject com.enterpriseweb/heroku-clojure-rest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.3.0"]
                 [simple-xhr "0.1.3"]
                 [cljs-ajax "0.2.3"]
                 [com.facebook/react "0.8.0.1"]
                 [com.enterpriseweb/open-stack-wrapper "0.1.1"]
                 [compojure "1.1.3"]
		 [ring/ring-core "1.2.1"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [enlive "1.1.5"]]
  :plugins [[com.cemerick/austin "0.1.3"]
            [lein-cljsbuild "1.0.1"]]
  :profiles {:dev {:repl-options { :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
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
                                                    :output-to "resources/public/app.js"
                                                    :optimizations :advanced
                                                    :pretty-print false
                                                    :preamble ["react/react.min.js"]
                                                    :externs ["react/externs/react.js"]}}]}}}
  :uberjar-name "heroku-clojure-rest-standalone.jar"
  :min-lein-version "2.0.0")
;ulimit -c unlimited
