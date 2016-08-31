(ns undead.cards.zip
  (:require [clojure.pprint :refer [pprint]]
            [clojure.zip :as z]
            [datascript.core :as d]
            [posh.reagent :as posh :refer [posh!]]
            [reagent.core :as r])
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
             :children/order [4 3]}
            {:db/id 3
             :node/text "Node 3"
             :children/order [2 5]}
            {:db/id 4
             :node/text "Node 4"
             :children/order [1]}
            {:db/id 5
             :node/text "Node 5"
             :children/order [1]}

            ])


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




(defn z-buttons [zatom]
  (fn [zatom]
    [:div
     [:button {:on-click #(swap! zatom z/next)}
      "next"]
     [:button {:on-click #(swap! zatom z/prev)}
      "prev"]
     [:button {:on-click #(swap! zatom z/up)}
      "up"]
     [:button {:on-click #(swap! zatom z/down)}
      "down"]
     [:button {:on-click #(swap! zatom z/remove)}
      "remove"]
     [:button {:on-click (fn [e]
                           (swap! zatom
                                  (fn [z]
                                    (z/insert-child z 1))))}
      "insert-child 1"]
     [:button {:on-click (fn [e]
                           (swap! zatom
                                  (fn [z]
                                    (z/append-child z 1))))}
      "append-child 1"]]
    ))



(defn ds-zip-view [conn]
  (let [zatom (r/atom (ds-zip conn 1))]
    (fn []
      [:div
       [z-buttons zatom]
       (pr-str @zatom)
       ])))



(defcard-rg nodea
  [ds-zip-view conn])


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
