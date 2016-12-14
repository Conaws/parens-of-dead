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
                                  )))))
 [1 2 3 4]
 [5])

((fn [x y]
   (first x))
 [1 2 3 4]
 [5])

;; interpose a sequence
((fn [x xs]
   (drop-last (flatten  (for [a xs]
                          [a x])))) 0 [1 2 3])

;;; (fn [x s] (butlast (mapcat #(list % x) s)))

;;; (fn [d s] (rest (mapcat #(list d %) s)))

;; better solution
(#(drop-last (interleave %2 (repeat %1)))
 0
 [1 2 3])


;; split a sequence without split-at

((fn [n s]
   (let [x (take n s)
         b (drop n s)]
     [x b]
     ))

 1
 [:a :b :c])

;; works with subvec
;; juxt works when applying against many args
((juxt #(subvec %2 0 %)
       #(subvec %2 %)) 1 [:a :b :c])
;; better solution -- (juxt take drop)


(= (set ((fn [x]
           (vals (group-by type x))
           )
         [1 :a 2 :b 3 :c])



        ) #{[1 2 3] [:a :b :c]})


((fn [& s]
   (true? (and (some true? s)
              (not (every? true? s)))))

 false
 false 
 )

;; alternatives are
;; (fn [s]
;; (and (boolean (some true? s))   (not-every? true? s ) ))


;; simplest was not=



;;flipping out
(fn [my-fn] 
  (fn [& args]
    (apply my-fn (reverse args))))

;; using comp #(comp (partial apply %) reverse list)

(fn [f]
  (comp (partial apply f) reverse list))



(hash-map :a :b :c :d)

;; create zipmap
((fn [a b]
   (apply hash-map (interleave a b)))


[1 2 3]
 [:a :b :c])


;; problem 66, Greatest common divisor

(mod 6 2)
(mod 4 2)

((fn [x y]
   (apply max (filter #(= 0 (mod y %))
               (filter #(= 0 (mod x %)) (range 1 (inc x))))))


 48
 24)


(fn [x y]
  (apply max (filter #(= 0 (mod y %) (mod x %))  (range 1 (inc (max x y))) )))



((fn [m]
   (into {} (apply concat (for [[k v] m]
                           (for [[k' v'] v]
                             [[k k'] v'])
                           ))))

 {:a {:b :c
      :c :d}
  :e {:f :g}}
 )




;; much nicer 
(fn [m]
  (->>
   (for [[k1 v1] m
   	     [k2 v2] v1]
     {[k1 k2] v2})
   (apply merge)))


((fn [m]
   (into {} (for [[k v] m
                  [k' v'] v]
              [[k k'] v'])))

 {:a {:b :c
      :c :d}
  :e {:f :g}}
 )
