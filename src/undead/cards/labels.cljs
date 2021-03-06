(ns undead.cards.labels
  (:require [cljs-time.core :refer [now]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [button v-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [e sample-nodes schema]])
  (:require-macros [devcards.core :refer [defcard-rg]]))

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



(defcard-rg labeldraw
  [v-box
   :gap "10px"
   :children
   [[button :label "hey"]
    [rc/title :label "title"
     :level :level1
     :underline? true]
    [button :label "ho"]
    [rc/scroller
     :v-scroll :auto
     :height "300px"
     :width "55px"
     :child [:div (repeat 1000 "lorem ipsum dolar")]]]
   ]
  )


(def datatom (r/atom (now)))

(defcard-rg tabledrawa
  [v-box
   :gap "10px"
   :children
   [[rc/datepicker-dropdown :model datatom
     :on-change #(reset! datatom %)]
    [rc/datepicker :model datatom
     :on-change #(reset! datatom %)]
    [rc/title :label "title"
     :level :level1
     :underline? true]
    [button :label "ho"]
    [rc/scroller
     :v-scroll :auto
     :height "300px"
     :width "55px"
     :child [:div (repeat 1000 "lorem ipsum dolar")]]]
   ]
  )











(defn barometer1
  ([score] (barometer1 score 30))
  ([score size]
   [:div  {:style {:width  (str size "px")
                   :height (str size "px")
                   :border "2px solid black"

                   :border-radius  (str size "px")}}
    [:div  {:style
            {:background-color "black"
             :width  (str size "px")
             :height (str size "px")
             :border-radius  (str size "px")
             :opacity (str (/ score 100))}}
     ]]))


(defn barometer2
  ([score] (barometer2 score 30))
  ([score size]
   [:div  {:style {:width  (str size "px")
                   :height (str size "px")
                   :border "2px solid black"
                   :overflow "hidden"
                   :border-radius  (str size "px")}}
    [:div  {:style
            {:background-color "black"
             :width  (str score "%")
             :height (str size "px")
             }}
     ]]))


(defn barometer3
  ([score] (barometer3 score 10 50))
  ([score width]  (barometer3 score 10 width))
  ([score height width]
   [:div  {:style {:width  (str width "px")
                   :height (str height "px")
                   :border "2px solid black"
                   :background-color "blue"
                   :overflow "hidden"
                   }}
    [:div  {:style
            {:background-color "white"
             :width  (str score "%")
             :height "100%"
             }}
     ]]))



(defcard-rg barometers
  [:div
   [barometer1 5]
   [barometer1 25 50]
   [barometer2 50 50]
   [barometer2 75 50]
   [barometer3 75 10 300]
   [barometer3 75 10 300]
   [barometer3 90 10 300]
   ])
