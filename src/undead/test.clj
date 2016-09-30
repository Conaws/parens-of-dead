(ns undead.test
  (:require  [clojure.test :as t]
             [clojure.string :as str]))


(def sampstring  "< Peter Thiel")


(str/starts-with? sampstring "<")

(defn build-trie [seed & kvs]
  (reduce
   (fn [trie [k v]]
     (assoc-in trie (concat k [:val]) v))
   seed
   (partition 2 kvs)))

(def trie (build-trie {} "f" :f "foo" :foo "bar" :bar))

(defn prefix-match [target trie]
  (when (seq target)
    (when-let [node (trie (first target))]
      (or (:val node)
          (recur (rest target) node)))))


(prefix-match "barstool" trie)
