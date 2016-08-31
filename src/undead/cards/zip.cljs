(ns undead.cards.zip
  (:require [clojure.pprint :refer [pprint]]
            [clojure.zip :as z]
            [datascript.core :as d]
            [posh.reagent :as posh :refer [posh!]])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(defcard-rg test
  [:div "hello"])

(enable-console-print!)

(def schema {:node/children {:db/valueType :db.type/ref
                             :db/cardinality :db.cardinality/many}})


(defonce conn (d/create-conn schema))
 (posh! conn)

(def nodes [{:db/id 1
             :node/text "Node 1"
             :children/order [2 3]}
            {:db/id 2
             :node/text "Node 2"
             :children/order [2 3]}
            {:db/id 3
             :node/text "Node 3"
             :children/order [2 3]}])


(d/transact! conn nodes)


(defcard-rg nodes
  [:div
   (pr-str @conn)])

(defn get-node [db id]
  (d/pull @db '[*] id))

(defn get-nodes [db ids]
  (d/pull-many @db '[*] ids))

(defn ds-zip [db id]
  (z/zipper
   :children/order
   #(get-nodes db (:children/order %))
   seq
   (get-node db id)))


(defcard-rg nodea
  [:div
   (pr-str (ds-zip conn 1))])


;(pprint (get-node conn 1))
(pprint (get-nodes conn [1 2]))

(pprint (ds-zip conn 1))

(defn ast-zip [data]
  (z/zipper :children
            (fn [node]
              (map node (:children node)))
            (fn [old new]
              (merge old
                     (zipmap (:children old)
                             new)))
            data)
  )
