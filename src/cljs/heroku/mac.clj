(ns heroku.mac
  )

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


(defmacro t
  "Sugar over reify for quickly putting together components that
   only need to implement om.core/IRender and don't need access to
   the owner argument."

  [in out-value  the-key]
  `(cljs.core.async.macros/go
     (let [[out# s#] (cljs.core.async/<! ~in)]
       (if (nil? out#)
         (throw (js/Error. "nil own-channel"))
         (do
           (cljs.core.async/>! out#  ~out-value)
           (cljs.core.async/<! (cljs.core.async/timeout 120))
           (if-let [published# (get  s# ~the-key)]
             (cljs.core.async/<! published#)
             (throw (js/Error. (str "the key " ~the-key " is not on available published" )))
             )))

       ))
)
;[out# {:keys [~key]}] `(<! ~in)
(macroexpand-1 '(t (cljs.core.async/chan) :connection :kkk))
