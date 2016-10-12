(ns undead.subs
  (:require [reagent.core :as r]))

(defmacro deftrack [name params* & body]
  `(def ~name
     (partial r/track (fn ~params* ~@body))))

#_(defmacro multi-filter [search-vec]
  (vector `(for [n search-vec
              :let [x ~(symbol (str '?p n))]]
          x)))



(defn handle-tags [arg-vec]
  (for [a arg-vec]
    (str '? a)))

(defmacro multi-filter [name arg-vec]
  `{:tag :domain
    :name (str '~name)
    :content [~@(handle-tags arg-vec)]}
  )



(defmacro mtest [name arg-vec]
  `{:tag :domain
    :test '~(symbol (str "?" "p"))
    :test2 '~(vec (for [a arg-vec]
                       (list (symbol "child")
                             (symbol (str '? a))
                             (symbol "?cid"))
                        ))
    :name (str '~name)
    :content [~@arg-vec]})

(defmacro mquery [f arg-vec]
  `{:find ~f
    :in '[~'$ ~'% ~(vec (for [a arg-vec]
                      (symbol (str '? a))))]
    :where '~(vec (apply concat (for [a arg-vec
                                      :let [x (symbol (str '? a))
                                            xid (symbol (str x "id"))]]
                              [[xid :title x]
                               (list (symbol "child")
                                     xid
                                     (symbol "?cid"))]
                              )))
    })
;; (defmacro multi-filter [search-vec]
;;   `[~(for [n search-vec
;;           :let [x (symbol (str ?p n))]]
;;       ('~child ~x '~?cid))])
