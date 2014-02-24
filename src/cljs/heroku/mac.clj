(ns heroku.mac)

(defmacro minimal
  "Sugar over reify for quickly putting together components that
   only need to implement om.core/IRender and don't need access to
   the owner argument."

  [in out-value]
  `(cljs.core.async.macros/go
     (let [[out# _] (cljs.core.async/<! ~in)]

       (cljs.core.async/>! out#  ~out-value)



       ))
  )


(defmacro alert [mes]
  `(js/alert "the message"))


(macroexpand '(alert "ooooo"))

(defmacro wurivagnuc
  "wait-until-ready-inject-value-and-get-next-unready-channel"

  [in out-value  the-key]
  `(cljs.core.async.macros/go

     (let [[out# s#] (cljs.core.async/<! ~in)]
       (if (nil? out#)
         (throw (js/Error. "nil own-channel"))
         (do
           (.log js/console "channel available")
           (cljs.core.async/>! out#  ~out-value)
           (.log js/console (str "channel written with value passed" ))
           (cljs.core.async/<! (cljs.core.async/timeout 120))
           (if-let [published# (get  s# ~the-key)]
             (do
               (.log js/console (str "next channel found with this id" ~the-key))
               (cljs.core.async/<! published#))
             (throw (js/Error. (str "the key " ~the-key " is not on available published" )))
             )))

       ))
)


(defmacro t
  "wait-until-ready-inject-value-and-get-next-unready-channel"

  [in out-value  the-key]
  `(cljs.core.async.macros/go

     (let [[out# s#] (cljs.core.async/<! ~in)]
       (if (nil? out#)
         (throw (js/Error. "nil own-channel"))
         (do
           (.log js/console "channel available")
           (cljs.core.async/>! out#  ~out-value)
           (.log js/console (str "channel written with " ~out-value))
           (cljs.core.async/<! (cljs.core.async/timeout 120))
           (if-let [published# (get  s# ~the-key)]
             (do
               (.log js/console (str "next channel found with this id" ~the-key))
               (cljs.core.async/<! published#))
             (throw (js/Error. (str "the key " ~the-key " is not on available published" )))
             )))

       ))
)

(defmacro listen-component
  [listener owner  om-component app opts]

  `(om.core/build ~om-component ~app (heroku.util/check-map ~opts (~listener (om/get-state ~owner :nexts))) )
  )



;[out# {:keys [~key]}] `(<! ~in)
;(macroexpand-1 '(t (cljs.core.async/chan) :connection :kkk))
