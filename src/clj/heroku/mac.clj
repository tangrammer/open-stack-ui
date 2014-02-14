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

       (cljs.core.async/>! out#  ~out-value)

       (cljs.core.async/<! (get  s# ~the-key))

       ))
)
;[out# {:keys [~key]}] `(<! ~in)
(macroexpand-1 '(t (cljs.core.async/chan) :connection :kkk))
