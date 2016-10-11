(ns undead.subs
  (:require [reagent.core :as r]))

(defmacro deftrack [name params* & body]
  `(def ~name
     (partial r/track (fn ~params* ~@body))))

;; (defmacro multi-filter [search-vec]
;;   (apply str (for [n search-vec
;;              :let [x (symbol (str '?p n))]]
;;          x)))
(defmacro multi-filter [search-vec]
  `[
    ~(for [n search-vec
          :let [x (symbol (str '?p n))]]
      ('child ~x '?cid))])
