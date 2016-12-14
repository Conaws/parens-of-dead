(ns user
  (:require [reloaded.repl :refer [reset stop system]]
            [undead.system]
            [clojure.string :as str]))

(reloaded.repl/set-init! #'undead.system/create-system)



;; fclojure http://www.4clojure.com/problem/solutions/29
(+ 1 1)
(defn x [x]
  (apply str 
         (filter #(re-matches #"[A-Z]" (str %)) x)))


(defn y [x]
  (reduce str (filter #(Character/isUpperCase %) x)))


(defn z [x]
  (apply str (re-seq #"[A-Z]" x)))

(x "aaa DDDelkD Bb")
(z "aaa DDDelkD Bb")


;; implement range

(let [x (fn [start end]
          (apply list (loop [acc [] n start]
                  (if (= end n)
                    acc
                    (recur (conj acc n) (inc n))
                    ))))

      b (fn [s e]
          (take-while #(< % e) (iterate inc s)))
      ]
  (b 1 40))




