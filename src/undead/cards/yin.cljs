(ns undead.cards.yin
  (:require [cljs-time.core :refer [now]]
            [clojure.set :as set]
            [clojure.string :as str]
            [com.rpl.specter :as sp :refer [ALL]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [v-box box h-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [e]]
            [undead.cards.grid :as grid]
            [clojure.string :as str])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack]]))



(def grid-transaction
  [{:db/id 1
    :node/type :set
    :node/title "People"
    :set/members [{:db/id -2
                   :node/title "Vannevar Bush"}
                  {:db/id -3
                   :node/title "Doug Engelbart"}]}
   {:db/id -4
    :node/type :set
    :node/title "Contacted"
    :set/members [[:node/title "Vannevar Bush"]
                  [:node/title "Doug Engelbart"]
                  ]}
   {:db/id -5
    :node/type :set
    :node/title "Scientists"
    :set/members [[:node/title "Vannevar Bush"]

                  ]}
   ])

(def simp-schema {:node/title        {:db/unique :db.unique/identity}
                   :set/down          {:db/valueType   :db.type/ref
                                          :db/cardinality :db.cardinality/many}})



(defonce test-db (d/db-with (d/empty-db simp-schema)
                        [{:db/id      1
                          :node/title "Books"}
                         {:db/id      2
                          :node/title "Science"}
                         {:db/id      3
                          :node/title "Vannevar Bush"}
                         {:db/id      4
                          :node/title "Doug Engelbart"}
                         {:db/id      5
                          :node/title "Favorite"}
                         {:db/id      6
                          :node/title "About"}
                         {:db/id           7
                          :node/title      "Doug's Favorite Books"
                         }
                         {:db/id      8
                          :node/title "Memex"
                          :set/_down     [3 4]}
                         {:db/id      9
                          :node/title "Conor"}
                         {:db/id      10
                          :node/title "Conor's Favorite Lists"
                          :set/_down     [9]
                          :set/down   [7]}
                         {:db/id      11
                          :node/title "As we may think"
                          :set/_down     #{3 7}
                          :set/down   #{8}}
                         ]))


(defonce conn2 (d/conn-from-db test-db))
(posh! conn2)

#_(defn q [& args]
  (r/track (apply d/q args)))

(def child-q
  '[:find ?title (count ?x)
    :where
    [?x :set/down ?e]
    [?e :node/title ?title]])

(def parent-q
  '[:find ?title (count ?e)
    :where
    [?x :set/down ?e]
    [?x :node/title ?title]])



(defn Lview [keyfn items i-fn]
  [:ul
   (for [i items]
     ^{:key (keyfn i)}[i-fn i])])

(defn Li [[a b]]
  [:li (str a " "b)])

(defn Li-parent [[a b]]
  [:li (str a) [:sub b]])

(defn Li-child [[a b]]
  [:li (str a) [:sup b]])


(def child-q2
  '[:find ?title (count ?y)
    :in $ [?parents ...]
    :where
    [?x :node/title ?parents]
    [?x :set/down ?e]
    [?e :node/title ?title]
    [?y :set/down ?e]])

(def parent-q2
  '[:find ?title (count ?y)
    :in $ [?children ...]
    :where
    [?x :node/title ?children]
    [?e :set/down ?x]
    [?e :node/title ?title]
    [?e :set/down ?y]])


;;; this shows that adding parents increases the range -- rather than filtering further
(defn d [conn]
  (let [filter-parents ["Vannevar Bush" "Doug Engelbart"]
        x (posh/q conn child-q2 filter-parents )
        ;; x (posh/q conn child-q2 )
        filter-children ["Memex" "As we may think"]
        y (posh/q conn parent-q2 filter-children)]
    (fn [conn]
      [:div.flex
       [:div
        [:h1 "Parents"]
        [:b (pr-str filter-children)]
        [Lview first @y Li-parent]]
       [:div
        [:h1 "Children"]
        [:b (pr-str filter-parents)]
        [Lview first @x Li-child]]
       [:div
        [:button {:on-click #(d/transact! conn [{:node/title (str (rand 100))
                                                 :set/_down [10]}])}
         "Hey, what gives"]]])))


(defcard-rg d-card
  [d conn2])
(defn c [conn]
  (let [filter-parents ["Vannevar Bush"]
        x (posh/q conn child-q2 filter-parents )
        ;; x (posh/q conn child-q2 )
        filter-children ["Memex"]
        y (posh/q conn parent-q2 filter-children)]
    (fn [conn]
      [:div.flex
       [:div
        [:h1 "Parents"]
        [:b (pr-str filter-children)]
        [Lview first @y Li-parent]]
       [:div
        [:h1 "Children"]
        [:b (pr-str filter-parents)]
        [Lview first @x Li-child]]
       [:div
        [:button {:on-click #(d/transact! conn [{:node/title (str (rand 100))
                                                 :set/_down [10]}])}
         "Hey, what gives"]]])))


(defcard-rg c-card
  [c conn2])

(defn b [conn]
  (let [x (posh/q conn child-q)
        y (posh/q conn parent-q)]
    (fn [conn]
      [:div.flex
       [:div
        [:h1 "Parents"]
        [Lview first @y Li-parent]]
       [:div
        [:h1 "Children"]
        [Lview first @x Li-child]]
       [:div
        [:button {:on-click #(d/transact! conn [{:node/title (str (rand 100))
                                                 :set/_down [10]}])}
         "Hey, what gives"]]])))


(defcard-rg bbb
  [b conn2])

(defcard-rg bb
  [b conn2])

(defn show-conn [conn]
  (fn [conn]
    [:div (pr-str conn)]))

(defcard-rg a
  [show-conn conn2])
