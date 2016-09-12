(ns undead.cards.logic
  (:require 
   [datascript.core :as d]
   [posh.core :as posh :refer [posh! pull q]]
   [re-com.core :as rc :refer [md-circle-icon-button]]
   [reagent.core :as r]
   [reagent.core :as reagent])
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


(def sample-nodes2
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


(def lconn2 (d/create-conn schema))
(posh! lconn2)
(d/transact! lconn2 sample-nodes2)


(defn render-node [conn id]
  (let [n (posh/pull conn '[*] id)
        used-in (posh/q conn '[:find ?e
                               :in $ ?id
                               :where [?e :set/members ?id]]
                        id)]
    (fn []
      [:div.flex 
       [md-circle-icon-button
        :md-icon-name "YO"
        :tooltip "hey?"
        :on-click #()]
       [:div (:node/title @n)]

       (if used-in
         [:sub
          [:button (pr-str (count @used-in))]])
       ]

      )))


(defcard-rg node-test
  [render-node lconn2 1])




(def icons
  [{:id "zmdi-plus"    :label [:i {:class "zmdi zmdi-plus"}]}
   {:id "zmdi-delete"  :label [:i {:class "zmdi zmdi-delete"}]}
   {:id "zmdi-undo"    :label [:i {:class "zmdi zmdi-undo"}]}
   {:id "zmdi-home"    :label [:i {:class "zmdi zmdi-home"}]}
   {:id "zmdi-hourglass"    :label [:i {:class "zmdi zmdi-hourglass"}]}
;   {:id "zmdi-group"    :label [:i {:class "zmdi zmdi-group"}]}
   {:id "zmdi-filter-list"    :label [:i {:class "zmdi zmdi-filter-list"}]}
   {:id "therefore"    :label [:i "∴"]}
   {:id "zmdi-account" :label [:i {:class "zmdi zmdi-account"}]}
   {:id "zmdi-info"    :label [:i {:class "zmdi zmdi-info"}]}])





(defn main []
  (let [selected-icon (r/atom (:id (first icons)))]
    (fn []
      [:div
       [rc/v-box
        :gap "8px"
        :children [
                   [rc/horizontal-bar-tabs
                    :model selected-icon
                    :tabs icons
                    :on-change #(reset! selected-icon %)]
                   [:div.rc-div-table-row
                    "a"
                    #_[rc/row-button
                       :md-icon-name a]
                    ]
                   [:div.rc-div-table-row
                    "a" ]]]

       ]

      ))
  
  )


(defcard-rg recomtest
  [main])


(defn select-node []
  (let [nodes (posh/q lconn2 '[:find ?e ?text
                               :where [?e :node/title ?text]])
        selection-id (r/atom nil)]
    (fn []
      (let [new-nodes (map (fn [[e t]]
                             {:id e :label (str e ":  "t)}) @nodes)
            sorted-nodes (sort-by :id new-nodes)]
        [:div.flex
        [rc/single-dropdown
         :choices sorted-nodes
         :placeholder "If this then that"
         :filter-box? true
         :width "200px"
           :model selection-id
           :on-change #(reset! selection-id %)]
        #_[:div (pr-str new-nodes)]]))))


  (defcard-rg selecttest
    [select-node])


(defn select-node2 []
  (let [nodes (posh/q lconn2 '[:find [?e ...]
                               :where [?e :node/title ?text]])
        selection-id (r/atom {})]
    (fn []
      (let [new-nodes (map (fn [e]
                             {:id e :label [:span e [:span
                                                  {:on-mouse-enter #(print "hey") }
                                                  "x"]]}) @nodes)
            sorted-nodes (sort-by :id new-nodes)]
        [:div.flex
         [rc/single-dropdown
          :choices sorted-nodes
          :model selection-id
          :on-change #(reset! selection-id %)]
         #_[:div (pr-str new-nodes)]]))))


(defcard-rg selecttest2
  [select-node2])


(defn add-atom [conn editatom]
  (do
    (d/transact conn [{:db/id -1 :node/title @editatom}])
    (reset! editatom "")
    ))



(defn node-input [conn]
  (let [editatom (r/atom "")]
    (fn []
      [:div
       [rc/input-text
        :model editatom
        :placeholder "Add an Atom"
        :on-change #(reset! editatom %)]
       [rc/button
        :label "Add"
        :on-click #(add-atom conn editatom )]
       ]
      
      ))
  )

(defcard-rg nodestest2
  [:div
   [node-input lconn2]
   [nodes-render lconn2]])


