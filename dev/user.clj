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



;;; factorials

(
 (fn [x]
   (apply * (range 1 (inc x)))
   )
 5)


;;interleave two seqs
((fn [x y]
   (let [
         b (into {} (map-indexed vector y))
         ]
     (flatten (take-while (comp not nil?) (for [[n a] (map-indexed vector x)
                                      :let [y (get b n)]]
                                  (if y
                                    [a y])
                                  )))

     )

   )

 [1 2 3 4]
 [5]
 )

((fn [x y]
   (first x)
   )

 [1 2 3 4]
 [5]
 )

;; interpose a sequence
((fn [x xs]
   (drop-last (flatten  (for [a xs]
                         [a x])))

   )

0
  [1 2 3]
  
 )
;;; (fn [x s] (butlast (mapcat #(list % x) s)))

;;; (fn [d s] (rest (mapcat #(list d %) s)))

;; better solution
(#(drop-last (interleave %2 (repeat %1)))
 0
 [1 2 3])





