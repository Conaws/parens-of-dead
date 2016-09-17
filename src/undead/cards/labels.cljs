(ns undead.cards.labels
  (:require [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]]
            [undead.subs :as subs :refer [qe e schema sample-nodes]]
            [re-com.core :as rc :refer [h-box md-circle-icon-button v-box popover-tooltip]]
            [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]])
  )

(defonce conn (d/create-conn schema))
(posh! conn)
(d/transact! conn sample-nodes)




(defmulti label (fn [conn n] (:node/type n)))


(defn deep-label [conn id]
  (label conn @(subs/pull-id conn id)))



(defmethod label :atom [conn n]
  (:node/title n))


(defmethod label :and [conn n]
  (str "AND("
       (map (partial deep-label conn) (map e (:set/members n)))
     ")"))

(defmethod label :or [conn n]
  (str "OR"
       (map (partial deep-label conn) (map e (:set/members n)))
       ))


(defmethod label :not [conn n]
  (str "NOT[[ " (deep-label conn (e (:logic/not n))) " ]]") )


(defmethod label :if [conn n]
  (str "IF :: " (deep-label conn (e (:logic/if n)))
  "THEN :: " (deep-label conn (e (:logic/then n))) )

  )

(defmethod label :certainty [conn n]
  (str
   (:certainty/score n)
   "% confidence that "
   (deep-label conn (e (:certainty/target n)))
   ))

(defmethod label :default [conn n]
  (:node/type n))


(defn node [conn id]
  (let [n (posh/pull conn '[*] id)]
    [:li (label conn @n)]))


(defn nodes [conn]
  (fn [conn]
    [:ul
     (for [n @(subs/node-feed conn)]
       ^{:key (subs/e n)} [node conn n])]))

(defcard-rg a
  [nodes conn])






(defmulti label-n (fn [conn n] (:node/type n)))


(defn deep-label-n [conn id]
  (label-n conn @(subs/pull-id conn id)))



(defmethod label-n :atom [conn n]
  (:db/id n))


(defmethod label-n :and [conn n]
       (map (partial deep-label-n conn) (map e (:set/members n)))
     )

(defmethod label-n :or [conn n]
   (vec
        (map (partial deep-label-n conn) (map e (:set/members n))))
       )


(defmethod label-n :not [conn n]
  (str "-'"(deep-label-n conn (e (:logic/not n))) "'") )


(defmethod label-n :if [conn n]
  (str  (deep-label-n conn (e (:logic/if n)))
       " -> " (deep-label-n conn (e (:logic/then n))) )

  )

(defmethod label-n :certainty [conn n]
  (str "<"
   (:certainty/score n)
   "% == "
   (deep-label-n conn (e (:certainty/target n)))
   ">"))

(defmethod label-n :default [conn n]
  (:node/type n))

(defn node-n [conn id]
  (let [n (posh/pull conn '[*] id)]
    [:li (pr-str (label-n conn @n))]))


(defn nodes-n [conn]
  (fn [conn]
    [:ul
     (for [n @(subs/node-feed conn)]
       ^{:key (subs/e n)} [node-n conn n])]))

(defcard-rg c
  [nodes-n conn])
