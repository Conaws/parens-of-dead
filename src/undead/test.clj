(ns undead.test
  (:require  [clojure.test :as t]
             [clojure.string :as str]
             [clojure.pprint :refer [pprint]]))


(def sampstring  "< Peter Thiel")


(str/starts-with? sampstring "<")

(defn build-trie [seed & kvs]
  (reduce
   (fn [trie [k v]]
     (assoc-in trie (concat k [:val]) v))
   seed
   (partition 2 kvs)))

(def trie (build-trie {} "for" :for "foo" :foo "bar" :bar))

(defn prefix-match [target trie]
  (when (seq target)
    (when-let [node (trie (first target))]
      (or (:val node)
          (recur (rest target) node)))))


(prefix-match "ford" trie)


;; (= false (str/starts-with? "   abc" "a"))


(defn title-parse [matcher title]
  (if (str/starts-with? title matcher)
    (let [newtitle (str/replace-first title matcher "")]
      (str/trim newtitle))   )
  )


(title-parse "p:" "p:Peter")


(defn newnode [title]
  (if-let [t (title-parse "<" title)]
    {:node/type :parent
     :node/title t}) )



(newnode "< Peter Thiel")


(defn divisible-by [number divisor]
  (zero? (mod number divisor)))

(defn say [n]
  (cond-> nil
    (divisible-by n 3) (str "Fizz")
    (divisible-by n 5) (str "Buzz")
    :else          (or (str n))))


(pprint (for [n (range 200)]
         (say n)))
