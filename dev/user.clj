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


(def a (iterate inc 0))




(defn my-iterate [f a]
  (cons (f a) (lazy-seq (my-iterate f (f a)))))


(def my-i (fn elper [f a]
            (lazy-seq (cons a (elper f (f a))))))


;; other option
;; #(reductions (fn [i _] (%1 i)) (repeat %2))

;; (take 5 (reductions + (range 3 100)))


(
 (fn [f a b]
   (cond
     (f a b) :lt
     (f b a) :gt
    :else :eq

     )
   )

 <
 400
 50
 )



;; group-by

(merge-with concat {:a [1]} {:a [2]})

((fn my-group-by [f s]
   (apply merge-with concat
          (for [a s]
            {(f a) [a]}))

   )
#(< 5 %)
[1 2 6]
 )

;; other solutions -- I like mine the best though
(fn [f s]
  (reduce (fn [m a]
            (let [x (f a)]
              (assoc m x (conj (get m x []) a)))) {} s))

(fn gb [f s]
  (reduce
   (fn [m x]
     (let [k (f x), v (m k)]
       (assoc m k ((fnil conj []) v x))))
   {} s))



;; infix calculator


((fn [& s]
   (:total (reduce
            (fn [base-map x]
              (if-let [a (:operator base-map)]
                {:total (a (:total base-map) x)}
                (assoc base-map :operator x))
              )

            {:total (first s)} (rest s)))
   )

 2
 +
 2
 -
 1
)


;; partition solutions
(fn [& xs]
  (reduce
   #((first %2) %1 (last %2))
   (first xs)
   (partition 2 (rest xs))))

(fn [x & ops]
  (reduce
   (fn [x [op operand]]
     (op x operand))
   x
   (partition 2 ops)))
;; real elegant multi-arity solutions

(fn me
  ([x f y] (f x y))
  ([x f y & r] (apply me (f x y) r)))


(fn infix
  ([]  0)
  ([x] x)
  ([x y z] (y x z))
  ([x y z & more]
   (apply infix (y x z) more)))



;; Product digits

((fn [x y]
   (->> (* x y)
       str
       seq
       (map (comp read-string str) )
       ))

 5
 5
 )

;; position in sequence

((fn [xs]
   (for [[b a] (map-indexed vector xs)]
     [a b])
   )

 [:a :b :c])

;; better solutions

#(map vector % (range))

;; map-indexed #(vector %2 %)


;; simple closures 107
;; my solution

(defn to-the [n]
  (fn raise [z]
    (reduce (fn [y _] (* y z)) 1 (range n))))

((to-the 3) 10)

((fn [n]
   (fn [ex]
     (reduce (fn [y x] (* y n)) 1 (range ex)))) 3)


;; better solution
#(let[pow %]
   (fn[x]
     (apply * (take pow (repeat x)))))


#(fn [x] (reduce * ( repeat % x)) )


;; Problem 55 -- count occurances

;; (= (__ [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
;; (update {1 1 2 0} 3 (fnil inc 0))
((fn [s]
   (reduce
    (fn [m x]
      (update m x (fnil inc 0))
      )
    {}
    s)))



(#(reduce (fn [m x]
             (assoc m x (inc (get m x 0))))
          {}
          %)


[1 1 2 3 2 1 1] )

;; shorter solution
;; reduce #(update-in % [%2] (fnil inc 0)) {}


;; (group-by identity [1 1 2 3 4 4  3 3 5])

;; #(let[m (group-by identity %)]
;;    (zipmap (keys m) (map count (vals m))))
;; #(into {} (for [[k v] (group-by identity %)] [k (count v)]))


;Write a function which returns a sequence of lists of x items each. Lists of less than x items should not be returned.



(fn [partition-size s]
  (loop [a [] p s]
    (if (= partition-size (count (take partition-size p)))
      (recur (conj a (take partition-size p)) (drop partition-size p))
      a
      )
    ))



;; solution using lazy-cat and take-while


(fn [w xs]
  (take-while #(= w (count %))
              ((fn lazypart [ys]
                 (lazy-cat [(take w ys)] (lazypart (drop w ys))))
               xs)))



;; dot product


((fn [s1 s2]
   (apply + (map (partial apply *)(partition 2 (interleave s1 s2)))))

 [1 2 3]
 [2 2 2]
 )


;; better solution

(#(reduce + (map * %1 %2))  [1 2 3][2 4 4])


;; pascals triangle


((fn [s]
   (loop [a [] xs s]
     (if (empty? xs)
       (conj a 1)
       (recur (conj a
                    (+ (or (last a) 0 )
                       (first xs)
                       ))
              (rest xs))
       )
     )
 )
[1 3 3 1]
 )


((fn next-triangle [s]
   (->> (reduce (fn [m x]
                 (let [new-m (assoc m :to-add x)]
                   (if-let [l (:to-add m)]
                     (update-in new-m [:m] conj (+ x l))
                     new-m
                     ))
                 )

               {:m []}
               s)
        :m
        (cons 1)
        vec
        ((fn [x]
           (conj x 1)
           ))
        ))
 [1]
 )


(defn next-triangle [s]
  (->> (reduce (fn [m x]
                 (let [new-m (assoc m :to-add x)]
                   (if-let [l (:to-add m)]
                     (update-in new-m [:m] conj (+ x l))
                     new-m)))
               {:m []} s)
       :m
       (cons 1)
       vec
       ((fn [x]
          (conj x 1)
          ))
       ))


(last (take 5 (iterate next-triangle [1])))


((fn [n]
   (last
    (take n
          (iterate
           (fn next-triangle [s]
             (->> (reduce (fn [m x]
                            (let [new-m (assoc m :to-add x)]
                              (if-let [l (:to-add m)]
                                (update-in new-m [:m] conj (+ x l))
                                new-m)))
                          {:m []} s)
                  :m
                  (cons 1)
                  vec
                  ((fn [x]
                     (conj x 1)
                     ))
                  ))
           [1])))
   )
9
 )


;; better pascal's triangle
(#(nth (iterate (fn [x] (concat [1]
                                (map + x (rest x))
                                [1]))
                [1]) (dec %))

 4
 )




;; pascal's trapezoid
(take 5
      ((fn ptrap [start]
    (iterate
     (fn next-triangle [s]
       (->> (reduce (fn [m x]
                      (let [new-m (assoc m :to-add x)]
                        (if-let [l (:to-add m)]
                          (update-in new-m [:m] conj (+' x l))
                          new-m)))
                    {:m []}
                    (concat [0] s [0]))
            :m))
     start))
  [1 2 1]
  ))

;; better pascal's trapezoid

(fn [row]
  (let [next-row #(map +' (concat [0] %) (concat % [0]))]
    (iterate next-row row)))


(defn get-b [n]
  (nth (iterate #(* 2 %) 1) n))



((fn [n]
   (->> n
        seq
        (map (comp read-string str))
        reverse
        (map-indexed vector)
        (filter (comp (partial = 1) second))
        (map first)
        (map
         (fn get-b [n]
           (nth (iterate #(* 2 %) 1) n))

         )
        (apply +)
        )

   )


 "1111")


((fn h [f s]
   (if (empty? s)
     []
     (lazy-seq (cons (f (first s)) (h f (rest s))))
     )
   )
 inc
(range 1001)
 )


;; better solution

(
 (fn [f xs]
   (reductions
    (fn [_ x] (f x))
    (f (first xs))
    (rest xs))
   )
 #(* 2 %)
 (range 20 30)
 )
;; (fn [f x] (rest (reductions #(f %2) nil x)))


((fn h [tree]
   (and (coll? tree)
        (= 3 (count tree))
        (every? (comp not false?)
                (map
                 #(if (coll? %)
                    (h %)
                    %)
                 tree
                 ))))
 [1 [2 [3 [4 false nil] nil] nil] nil]
 )


;; better solution

(fn tree? [coll]
  (if (coll? coll)
    (if (= (count coll) 3)
      (and (tree? (second coll)) (tree? (last coll)))
      false)
    (not (false? coll))))



;;; beauty is symmettry
;; [1 [2 nil [3 [4 [5 nil nil] [6 nil nil]] nil]]
;;  [2 [3 nil [4 [6 nil nil] [5 nil nil]]] nil]]

;; step 1, take a tree and reverse it



;; ++ +-------------+--------------------------------+----------+
;; || |-------------||||||||||+--|+      ||||||||    |  | |     |
;; || |-------------||+||||||||----------||||||||    |  | |     |
;; ||+|-------------||-|||||||+----------+|||||||    |  | |     |
;; ||+|-------------||-|+||||||-----------||||||+    |  | |     |
;; || |-------------||-|-||||||-----------||||||-----|--+ |     |
;; || +-------------+--------------------------------+----+     |
;; ||       +--------------++ |-------------|-------------------+
;; ||                         +-------------+
;; ++

((fn [tree]
   (let [flip (fn [[root l r]]
                [root r l])]
     (flip tree)
     ))


 [1 [2 nil [3 4 nil]]
  [2 [3 nil 4] nil]]
 )


((fn symmetrical? [tree]
   (let [df (fn deep-flip [tree]
           (if (coll? tree)
             (let [[root l r] tree]
               [root
                (deep-flip r)
                (deep-flip l)])
             tree)
              )]
     (= tree
      (df tree))
     ))

 [1 [2 nil [3 4 nil]]
  [2 [3 nil 4] nil]
  ]
 )


;; using letfn
(fn
  [t]
  (letfn [(mirror [t] (if (nil? t)
                        nil
                        [(first t)
                         (mirror (nth t 2))
                         (mirror (nth t 1))]))]
    (= t (mirror t))))

;; #(letfn [(mirror [tree]
;;            (if (nil? tree) nil
;;                [(first tree)
;;                 (mirror (second (rest tree)))
;;                 (mirror (second tree))]))]
;;    (=
;;     (flatten (second %1))
;;     (flatten (mirror (second (rest %1))))))


;; playing cards

(apply hash-map (concat ["J" 9 "Q" 10 "K" 11 "A" 12] (interleave (range 8) (range 2 10))))

(apply sorted-map (concat ["J" 9 "Q" 10 "K" 11 "A" 12] (interleave(map str (range 2 11)) (range 10))))

(defn suit [card]
  (let [suit-map {"D" :diamond
                  "S" :spade
                  "C" :club
                  "H" :heart}
        rank-map (apply sorted-map (concat ["T" 8 "J" 9 "Q" 10 "K" 11 "A" 12] (interleave(map str (range 2 10)) (range 9))))

        [_ s r]
        (re-find #"(D|S|C|H)([0-9]+|[TQJKA])" card)]
    {:rank (rank-map r)
     :suit (suit-map s)}))

(map
(comp :rank suit str)
 '[S2 S3 S4 S5 S6 S7 S8 S9 ST SJ]

 )

