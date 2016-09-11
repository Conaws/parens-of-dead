(ns undead.cards.logic
  (:require 
            [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]]
            [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(enable-console-print!)

(def schema {:set/members {:db/valueType :db.type/ref
                             :db/cardinality :db.cardinality/many}
             :certainty/target  {:db/valueType :db.type/ref
                                 :db/cardinality :db.cardinality/one}
             :logic/not  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}
             :logic/then  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}
             :logic/if  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}})


(def lconn (d/create-conn schema))
(posh! lconn)


(def sample-nodes
  [{:db/id      1
    :node/title "All Men are Mortal"}
   {:db/id      2
    :node/title "Socrates is a Man"}
   {:db/id      3
    :node/title "Socrates is Mortal"}
   {:db/id      4
    :logic/type :not
    :node/title "Not All Men are Mortal"
    :logic/not 1}
   {:db/id       5
    :logic/type  :and
    :logic/title  "A and B"
    :set/members #{1 2}}
   {:db/id      6
    :logic/type :if-then
    :logic/title "If (A and B) then C"
    :logic/if   5
    :logic/then 3}
   {:db/id 7
    :logic/type :or
    :logic/title "(B or C)"
    :set/members #{2 3}}
   {:db/id 8
    :node/title "D"}
   {:db/id 9
    :logic/type :or
    :logic/title "(B or C) or A"
    :set/members #{1 7}}
   {:db/id 10
    :logic/type :if-then
    :logic/title "If ((B or C) or A) then D"
    :logic/if 9
    :logic/then 8}
   {:db/id 11
    :certainty/score 90
    :certainty/target 1}
   {:db/id 12
    :certainty/score 50
    :certainty/target 2}
   {:db/id 13
    :certainty/score 40
    :certainty/target 3}
   ])


(d/transact! lconn sample-nodes)

(declare logic-node)

(defn and-render [conn id]
  (let [node (pull conn '[:node/title {:set/members ...}] id)]
    (fn []
       [:div.flex.bblack
        {:style
         {:border "1px dotted red"
          :padding "15px"
          :justify-content "space-between"
          :align-items "space-between"
          :background-color "#f1f1f1"}}
        "AND "
        (for [m (:set/members @node)]
          [logic-node conn (:db/id m)])])
    ))

(defn or-render [conn node]
    (fn [conn node]

       [:div.flex
        {:style
         {:border "1px dotted blue"
          :padding "15px"
          :justify-content "space-between"
          :align-items "space-between"
          :background-color "#f1f1f1"}}
        (for [m (pop (vec (interleave (:set/members node) (repeat :or))))]
          (if (= m :or)
            [:div " or "]
            [logic-node conn (:db/id m)])
          )])
    )


(defn if-then [conn node]
  (fn [conn node]
    [:div {:style
           {
            :background-color "#A2A2A2"}}
     [:div.flex {:style
                 {:justify-content "space-between"
                  :background-color "#A2A2A2"}}
      [:span "IF:"]
      [logic-node conn (:db/id (:logic/if node))]
      [:span "Then:"]
      [logic-node conn (:db/id (:logic/then node))]
      ]
     ]))

(defn logic-node [conn id]
  (let [i (pull conn '[*] id)
        flipped? (r/atom true)]
    (fn [conn]
      [:div
       {:style {:background-color
                "#d8d8d8"
                :padding "5px"
                :margin "2px"
                :border "2px solid #9b9b9b"}
        }
       [:div
        (if @flipped?
          [:div.flex
           {:style {:flex-flow "column wrap"}}
           [:sub.small
            {:on-click #(swap! flipped? not)}
            id]
           (condp = (:logic/type @i)
             :and [and-render conn id]
             :or [or-render conn @i]
             :if-then [if-then conn @i]
             (:node/title @i))]
          [:button
           {:on-click #(swap! flipped? not)}
           id]
          )]])))

(defn slider [attr conn id]
  (let [itm (pull conn '[*] id)]
    (fn [attr conn id]
      [:div
       [:input {:type "range"
                :style {:display "flex"}
                :name "start"
                :value (get @itm attr 0)
                :min 0
                :max 100
                :step 1
                :on-change (fn [e]
                             (do #_(js/alert
                                    (js/parseInt (-> e .-target .-value)))
                                 (d/transact! conn [{:db/id id attr
                                                     (js/parseInt (-> e .-target .-value))}])))}]])))

(defn- add-certainty [conn id]
  (d/transact! conn [{:db/id -1
                      :certainty/target id
                      :certainty/score 50}]))

(defn basic-node [conn i]
  (let [id (:db/id i)
        certainty (q conn '[:find [?cert-id]
                            :in $ ?itemid
                            :where [?cert-id :certainty/target ?itemid ]
                            ]
                     (:db/id i))]
    (fn []
      [:div
       [:button
        {:draggable true}
        (str (:db/id i) " "
             (pr-str (or (:logic/title i)
                         (:node/title i))))]

       (if-let [[cid] @certainty]
         [slider :certainty/score conn cid]
         [:button {:on-click #(add-certainty conn id)} :eval])]
      )))


(defn nodes-render [conn]
      (let [n (q conn '[:find ?eid ?val ?text
                   :where [?eid ?val ?text ]]
                 )
            all-ents (q conn '[:find (pull ?e [* {:set/members ...}])
                          :where  [?e]]
                       ) ]
        (fn [conn]
          [:div
           {:style
                      {:display "grid"
;                       :background-color "blue"
                       :grid-row-gap "5px"
                       :grid-template-areas "
'other .. .. .. items'
'.. .. .. .. items'
'.. .. .. .. items'

"
                       }}
           [:div {:display "flex"
                  :flex-flow "column wrap"}
            (for [[i] (sort-by (fn [[i]] (:db/id i)) @all-ents)]
              [logic-node conn (:db/id i)])]
           [:div {:style {:border "2px solid blue"
                          :display "flex"
                          :background-color "blue"
                          :padding "5px"
                          :color "white"
                          :overflow "scroll"
                          :flex-flow "column wrap"
                          :width "150px"
                          :grid-area "items"}}
            (for [[i] (sort-by (fn [[i]] (:db/id i)) @all-ents)]
              ^{:key i}[basic-node conn i])]]
          )))


(defcard-rg nodestest
  [nodes-render lconn])



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







(defn render-node [conn id]
  (let [n (posh/pull conn '[*] id)
        used-in (posh/q conn '[:find ?e
                               :in $ ?id
                               :where [?e :set/members ?id]]
                        id)]
    (fn []
      [:div (pr-str @n)
       (if used-in
         [:sub
          [:button (pr-str (count @used-in))]])
       ]

      )))



(defcard-rg node-test
  [render-node lconn 1])








