(ns undead.subs)

(defmacro deftrack [name params* & body]
  `(def ~name
     (partial r/track (fn ~params* ~@body))))


