(ns undead.cards.zip
  (:require [clojure.pprint :refer [pprint]]
            [clojure.zip :as z]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
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
             :children/order [4]}
            {:db/id 3
             :node/text "Node 3"
             :children/order [5]}
            {:db/id 4
             :node/text "Node 4"
            }
            {:db/id 5
             :node/text "Node 5"
             }

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
     [:button {:on-click #(swap! zatom z/right)}
      "right"]
     [:button {:on-click #(swap! zatom z/left)}
      "left"]
     [:button {:on-click #(swap! zatom z/remove)}
      "remove"]
     ]
    ))

(defonce zatom1 (r/atom (ds-zip conn 1)))

(defn ds-zip-view [zatom]
    (fn [zatom]
      [:div
       [z-buttons zatom]
       (pr-str @zatom)
       (let [[n zstruc :as z] @zatom]
         (if z
           [:div {:style {:display "grid"
                          :background-color "blue"
                          :grid-column-gap "60px"
                          :grid-row-gap "60px"
                          :grid-template-rows "120px 120px 120px"
                          :grid-template-areas "'.. parent .. '
'left node right'
'.. children ..'
"
                          }}
            [:div {:style {:grid-area "parent"}}
             (pr-str (:pnodes zstruc))
             ]
            [:div {:style {:grid-area "left"}}
             (pr-str (:l zstruc))]
            [:div.cell {:style {:grid-area "node"}}
             (pr-str n)] 
            [:div {:style {:grid-area "right"}}
             (pr-str (:r zstruc))]
            (if (z/branch? z)
              [:div {:style {:grid-area "children"}}
                                        (pr-str (z/children z))])])
         )
       ]))


(defcard-rg nodea
  [ds-zip-view zatom1]
  zatom1
  {:inspect-data true
   :history true})


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
