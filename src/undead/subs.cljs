(ns undead.subs
  (:require [datascript.core :as d]
            [posh.core :as posh]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [undead.util :refer [ssolo]])
  (:require-macros [undead.subs :refer [deftrack]]))

(def schema {:node/title {:db/unique :db.unique/identity}
             :set/members {:db/valueType :db.type/ref
                           :db/cardinality :db.cardinality/many}
             :certainty/target  {:db/valueType :db.type/ref
                                 :db/cardinality :db.cardinality/one}
             :logic/not  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}
             :logic/then  {:db/valueType :db.type/ref
                           :db/cardinality :db.cardinality/one}
             :logic/if  {:db/valueType :db.type/ref
                         :db/cardinality :db.cardinality/one}})



(def sample-nodes
  [{:db/id      1
    :node/type :atom
    :node/title "All Men are Mortal"}
   {:db/id      2
    :node/type :atom
    :node/title "Socrates is a Man"}
   {:db/id      3
    :node/type :atom
    :node/title "Socrates is Mortal"}
   {:db/id      4
    :node/type :not
    :node/title "Not All Men are Mortal"
    :logic/not 1}
   {:db/id       5
    :node/type  :and
    :logic/title  "A and B"
    :set/members #{1 2}}
   {:db/id      6
    :node/type :if
    :logic/title "If (A and B) then C"
    :logic/if   5
    :logic/then 3
    :set/members #{5 3}
    }
   {:db/id 7
    :node/type :or
    :logic/title "(B or C)"
    :set/members #{2 3}}
   {:db/id 8
    :node/type :atom
    :node/title "D"}
   {:db/id 9
    :node/type :or
    :logic/title "(B or C) or A"
    :set/members #{1 7}}
   {:db/id 10
    :node/type :if
    :logic/title "If ((B or C) or A) then D"
    :logic/if 9
    :logic/then 8}
   {:db/id 11
    :node/type :certainty
    :certainty/score 90
    :certainty/target 1}
   {:db/id 12
    :node/type :certainty
    :certainty/score 50
    :certainty/target 2}
   {:db/id 13
    :node/type :certainty
    :certainty/score 40
    :certainty/target 3}
   ])



(defprotocol Eid
  "A protocol for retrieving an object's entity id."
  (e [_] "identifying id for a value"))

(extend-protocol Eid
  number
  (e [n] n)

  cljs.core.PersistentHashMap
  (e [ent] (:db/id ent))

  cljs.core.PersistentArrayMap
  (e [ent] (:db/id ent))

  datascript.impl.entity.Entity
  (e [ent] (:db/id ent)))


(defn qe [query db & args]
  (when-let [result (-> (apply d/q query db args) ssolo)]
    (d/entity db result)))



(deftrack tqe [query db & args]
  (when-let [result (-> (apply d/q query db args) ssolo)]
    (d/entity db result)))



(defn all-nodes [conn]
  (posh/q conn '[:find [?e ...]
                 :where [?e :node/type]]))

(defn pull-id [conn id]
  (posh/pull conn '[*] id))


(deftrack sorted-nodes [conn]
  (sort @(all-nodes conn)))

(deftrack node-feed [conn]
  (reverse (sort @(all-nodes conn))))


(defn node-type [conn id]
  (posh/pull conn [:node/type] id))


(defn node-parents [conn id]
  (posh/pull conn [:set/_members :logic/_if :logic/_then] id))


(defn node-children [conn id]
  (posh/pull conn [:set/members :logic/if :logic/then] id))


(defn schema-pull [depth]
  [:node/title
   :node/type
   :logic/not
   :logic/if
   :logic/then
   :db/id
   {:set/members depth}])

(defn nodes-deep [conn depth]
  (posh/q conn '[:find [(pull ?e
                              ?pull) ...]
                 :in $ ?pull
                 :where [?e]]
          (schema-pull depth)))


;;;; re-frame subs
;;;; selections is a 

(reg-sub
 :get-in
 (fn [db [_ p]]
   (get-in db p)))

(reg-sub
 :selections
 (fn [db]
   (:selections db [])))


(reg-event-db
 :update-in
 (fn [db [_ p f]]
   (update-in db p f)))


(defn conj-in-path [p v]
  (dispatch [:update-in p (fn [e] (conj e v))]))
