(ns heroku.connections
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [heroku.util :as util]
   [ajax.core :refer [GET POST]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.browser.repl]
   [cljs.core.async :refer [put! chan <!]])
  )

(def username "facebook1428467850")
(def password "3a34gc72")
(def url "http://8.21.28.222:5000")


(defn base [data owner]
  (reify
    om/IRender
    (render [this]

      (dom/form #js {:className "form-signin" :role "form" }

                (dom/h2 #js {:className "form-signin-heading"} "Try a connection")
                (dom/label nil (str "Local VM http://192.168.56.102:5000" " " url))
                (dom/input #js {:ref "url" :type "text" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )
                (dom/label nil (str "admin" " " username))
                (dom/input #js {:ref "username" :type "text" :className "form-control" :placeholder "User Name" :required true } )
                (dom/label nil (str "password" " " password))
                (dom/input #js {:ref "password" :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(js/alert "connecting base!")} "Connect!")
                ))))

(defn tenant [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/form #js {:className "form-signin" :role "form" }
                (dom/h2 #js {:className "form-signin-heading"} "Connect to Tenant")
                (dom/label nil (str "Local VM http://192.168.56.102:5000" " " url))
                (dom/input #js {:ref "url" :type "url" :className "form-control" :placeholder "Connection Url" :required true :autoFocus true } )
                (dom/label nil (str "admin" " " username))
                (dom/input #js {:ref "username" :type "text" :className "form-control" :placeholder "User Name" :required true } )
                (dom/label nil (str "password" " " password))
                (dom/input #js {:ref "password" :type "password" :className "form-control" :placeholder "Password" :required true }  )
                (dom/label nil (str "admin/demo" " " username))
                (dom/input #js {:ref "tenant" :type "text" :className "form-control" :placeholder "Tenant name" :required true }  )
                (dom/button #js {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick #(js/alert "try-to-connect" )} "Connect!")
                ))))
