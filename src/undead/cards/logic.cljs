(ns undead.cards.logic
  (:require [clojure.string :as str]
            [com.rpl.specter :as sp]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]]
            [re-com.core
             :as
             rc
             :refer
             [h-box md-circle-icon-button popover-tooltip v-box]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [reagent.core :as r]
            [undead.cards.labels :as label]
            [undead.subs :as subs :refer [schema
                                          sample-nodes
                                          e qe]]
            [undead.util :refer [p]])
  (:require-macros
   [com.rpl.specter.macros :refer [select-one]]
   [devcards.core :refer [defcard-rg]]))

(enable-console-print!)

(def lconn (d/create-conn schema))
(posh! lconn)
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
            ^{:key (str m "or")}[logic-node conn (:db/id m)])
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
              ^{:key (str i "b")}[logic-node conn (:db/id i)])]
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
              ^{:key (str i "a")}[basic-node conn i])]]
          )))


(defcard-rg nodestest
  [nodes-render lconn])

(def sample-nodes2 subs/sample-nodes)


(defonce lconn2 (d/create-conn schema))
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
        selection-id (r/atom (set []))]
    (fn []
      (let [new-nodes (map (fn [e]
                             {:id e :label [:li e [:button
                                                  {:on-mouse-enter #(print "hey") }
                                                  "x"]]}) @nodes)
            sorted-nodes (r/atom (sort-by :id new-nodes))]
        [v-box
         :children[
                   [rc/selection-list
                    :choices sorted-nodes
                    :model selection-id
                    :on-change #(reset! selection-id %)]
                   #_[:div (pr-str new-nodes)]]]))))


(defcard-rg selecttest2
  [select-node2])


(defn add-atom [conn editatom]
  (do
    (d/transact conn [{:db/id -1
                       :node/type :atom
                       :node/title @editatom}])
    (reset! editatom "")
    ))

(def logic-types
  [{:id :logic/atom    :label "⦿" }
   {:id :logic/if  :label "⊩"}
   {:id :logic/and  :label "AND"}
   {:id :logic/or    :label "OR"}
   ])



(declare and-form or-form)


(defn node-input [conn]
  (let [editatom (r/atom "")
        selected-type (r/atom (:id (first logic-types)))]
    (fn []
      [rc/h-box
       :align :center
       :justify :start
       :class "bblack"
       :gap "10px"
       :children [[rc/vertical-bar-tabs
                   :model selected-type
                   :tabs logic-types
                   :on-change #(do
                                 (dispatch [:clear-selections])
                                 (reset! selected-type %))]
                  (condp = @selected-type
                    :logic/atom
                    [v-box
                     :children [[rc/title :label "ATOM"
                                 :level :level1
                                ]
                                [rc/input-text
                                 :model editatom
                                 :placeholder "Add an Atom"
                                 :on-change #(reset! editatom %)]
                                [rc/button
                                 :label "Add"
                                 :on-click #(add-atom conn editatom )]]]
                    :logic/or
                    [or-form conn]
                    :logic/and
                    [and-form conn]
                    :logic/if
                    [rc/title :label "IF"
                     :level :level1]
                    (pr-str @selected-type)
                    )]
       ]
      ))
  )




(defcard-rg inputtest
  [node-input lconn2])

(defn feed [conn]
  (let [all-ents(q conn '[:find (pull ?e [* {:set/members ...}])
                          :where  [?e]]
                   )]
    [v-box
     :gap "20px"
     :class "bblack"
     :children (vec
               (for [[i] (reverse (sort-by (fn [[i]] (:db/id i)) @all-ents))]
                 ^{:key (str i "b")}

                 [rc/box
                  :child
                  [:div (pr-str i)]]))]))

(defcard-rg feedtest
  [:div
   [node-input lconn2]
   [feed lconn2]])


(defn multi-drop1 [nodes]
  (let [nodes nodes
        selections (r/atom [])
        selection-id (r/atom nil)]
    (fn []
      (let [new-nodes (keep (fn [[e t ty]]
                              (if (not ((set @selections)
                                        e))
                                {:id e :label (str e ":  "t)
                                 :group (str ty) }
                                )) @nodes)
            sorted-nodes (sort-by :group new-nodes)]
        [v-box
         :children [[h-box
                     :children (vec (map (fn [e]
                                          [:div.well
                                           e
                                           [:span.label.label-warning
                                            {:on-click #(reset! selections
                                                                (vec (remove #{e} @selections ))
                                                                )}
                                            "X"]
                                           ]
                                          )  @selections))]
                    [rc/single-dropdown
                     :choices sorted-nodes
                     :placeholder "If this then that"
                     :filter-box? true
                     :width "200px"
                     :model selection-id
                     :on-change #(do
                                   (reset! selection-id nil)
                                   (swap! selections conj %))]]
         #_[:div (pr-str new-nodes)]]))))


(defn select-text-nodes []
  [multi-drop1
   (posh/q lconn2 '[:find ?e ?text ?ty
                    :where [?e :node/title ?text]
                    [?e :node/type ?ty]])])


(defcard-rg multi
  [select-text-nodes])


(defn w-spaces [s]
  (interleave (repeat " ") s))


(defn node-label [n]
  (condp = (:node/type n)
    :atom (str (:db/id n) ":  " (:node/title n))
    :not (str (:db/id n) ":  ~" (:db/id (:logic/not n)))
    :and (apply str (w-spaces (map :db/id (:set/members n))))
    :or (apply str (w-spaces (map :db/id (:set/members n))))
    :certainty nil
    (pr-str n))
  )

(declare small-drop)

(defn multi-drop [conn]
  (let [nodes (posh/q conn '[:find [(pull ?e [:node/title
                                              :node/type
                                              :logic/not
                                              :logic/if
                                              :logic/then
                                              :db/id
                                              {:set/members 3}] ) ...]
                               :where [?e]])
        selections (r/atom [])
        selection-id (r/atom nil)]
    (fn []
      (let [nodes  (keep (fn [m]
                           (if (not ((set @selections)
                                     (:db/id m)))
                             m)) @nodes)
            sort-2 (sort-by :node/type nodes)]
  ;      [:div (pr-str @nodes)]

        [v-box
         :children [[h-box
                     :children (vec (map (fn [e]
                                          [:div.well
                                           e
                                           [:span.label.label-warning
                                            {:on-click #(reset! selections
                                                                (vec (remove #{e} @selections ))
                                                                )}
                                            "X"]
                                           ]
                                          )  @selections))]
                    [rc/single-dropdown
                     :choices sort-2
                     :placeholder "If this then that"
                     :filter-box? true
                     :id-fn :db/id
                     :label-fn node-label
                     :group-fn #(str (:node/type %))
                     :width "200px"
                     :model selection-id
                     :on-change #(do
                                   #_(reset! selection-id nil)
                                   (swap! selections conj %))]]
         #_[:div (pr-str new-nodes)]]))))


(defcard-rg multitest2
  [multi-drop lconn2])





(print (random-uuid))





(defn remove-buttons [conn selection-atom remove-fn]
  (let [selections selection-atom]
    (fn []
      [v-box
       :children (vec (map (fn [e]
                             [:div
                              (label/deep-label-n conn e)
                              [:button.btn.btn-default.pull-right
                               {:on-click #(dispatch [:remove-from-selections e])} "X"]])

                           @selections))])))


(defn multi-drop-rf [conn]
  (let [nodes (subs/nodes-deep conn 1)
        selections (subscribe [:selections])
        selection-id (r/atom nil)
        ]
    (fn []
      (let [nodes  (keep (fn [m]
                           (if (not ((set @selections)
                                     (:db/id m)))
                             m)) @nodes)
            sort-2 (sort-by :node/type nodes)]
        [v-box
         :children [
                    [rc/single-dropdown
                     :choices sort-2
                     :placeholder "If this then that"
                     :filter-box? true
                     :id-fn :db/id
                     :label-fn node-label
                     :group-fn #(str (:node/type %))
                     :width "200px"
                     :model selection-id
                     :on-change #(do
                                   (subs/conj-in-path [:selections] %))]]]))))


(defcard-rg with-subs
  [multi-drop-rf lconn2])






(defn and-form [conn]
  (let [selections (subscribe [:selections])]
    (fn [conn]
      [:div.flex {:style {:align-items "center"
                          :flex-flow "column wrap"
                          }}
       [rc/title :level :level1
                  :label "AND"]
       [multi-drop-rf conn]
       [remove-buttons conn selections]
       [rc/button :label "Create"
        :on-click #(do
                     (d/transact! conn [{:db/id -1
                                         :node/type :and
                                         :set/members @selections}])
                     (dispatch [:clear-selections]))]
       ]
      )))


(defcard-rg and-formtest
  [and-form lconn2])


(defn or-form [conn]
  (let [selections (subscribe [:selections])]
    (fn [conn]
      [:div.flex {:style {:align-items "center"
                          :flex-flow "column wrap"
                          }}
       [rc/title :level :level1
        :label "OR"]
       [multi-drop-rf conn]
       [remove-buttons conn selections]
       [rc/button :label "Create"
        :on-click #(do
                     (d/transact! conn [{:db/id -1
                                       :node/type :or
                                         :set/members @selections}])
                     (dispatch [:clear-selections])
                                  )]
       ]
      )))


(defn simple-node [conn id]
  (let [n (posh/pull conn '[*] id)
        open (r/atom false)]
    [:li.list-group-item
     [:span id]

     (if-let [c (:node/title @n )]
        c ) 
     (when @open [:div (pr-str @(subs/node-parents conn id))])
     (when-let [c @(subs/node-children conn id)]
                  (if-let [i (:logic/if c)]
                    [:ul.indent
                    [:div.indent
                     [:b "IF "]
                     [simple-node conn (e i)]]
                     (let [b (:logic/then c)]
                      [:div.indent
                       [:b "then"]
                       [simple-node conn (e b)]
                       ])]
                    (when-let [lif (:set/members c)]
                      [:ul
                       [:b (pr-str (:node/type @n))]
                       (for [[i c] (map-indexed vector lif)]
                         ^{:key (str id i c)} [simple-node conn (e c)]
                         )]))
                 )
     ]))







(defn popdiv
  "Returns the markup for a basic button"
  [tooltip label]
  (let [showing? (r/atom false)]
    (fn
      [tooltip label]
      (when-not tooltip (reset! showing? false)) ;; To prevent tooltip from still showing after button drag/drop
      (let [
            the-button [:div
                        (merge
                          {:class    (str "rc-button btn " )
                           }
                          (when tooltip
                            {:on-mouse-over #(do (reset! showing? true))
                             :on-mouse-out  #(do (reset! showing? false))})
                          )
                        label]]
        
        [rc/box
         :class "display-inline-flex"
         :align :start
         :child (if tooltip
                  [popover-tooltip
                   :label    tooltip
                   :position :below-center
                   :showing? showing?
                   :anchor   the-button]
                  the-button)]))))






(defcard-rg poptest
[popdiv "boom" [:div "boooo"]]


  )







(defn simple-node2 [conn id]
  (fn [conn id]
    (let [n (posh/pull conn '[*] id)
          children (subs/node-children conn id)
          parents (subs/node-parents conn id)
          open (r/atom false)]
      [:li.list-group-item
       [:span.badge.pull-left id]
       (if-let [c (:node/title @n )]
         c
         ". ") 
       [rc/hyperlink
        :label (str (count @children))
        :class "badge pull-right"
        ]
       
       [:span.badge (count @children)]
       (when @open [:div (pr-str @parents)
                    (when-let [c @children]
                      (if-let [i (:logic/if c)]
                        [:ul.indent
                         [:div.indent
                          [:b "IF "]
                          [simple-node conn (e i)]]
                         (let [b (:logic/then c)]
                           [:div.indent
                            [:b "then"]
                            [simple-node conn (e b)]
                            ])]
                        (when-let [lif (:set/members c)]
                          [:ul
                           [:b (pr-str (:node/type @n))]
                           (for [[i c] (map-indexed vector lif)]
                             ^{:key (str id i c)} [simple-node conn (e c)]
                             )]))
                      )])
       ])))

(defcard-rg simpletest2
  [simple-node2 lconn2 10])


(defn simple-nodes [conn]
  (fn [conn]
    [:div
     (for [n @(subs/node-feed conn)]
       ^{:key (subs/e n)} [simple-node conn n])]))

(defn simpler-nodes [conn]
  (let [nodes (subs/all-nodes conn)]
    (fn [conn]
      [:ul.bs-docs-sidenav
       (for [i @nodes]
         ^{:key (str i "b")}
         [logic-node conn i])])))

(defcard-rg simplestest2
 [simple-nodes lconn2])

(defcard-rg simplestest
  [:div.nav.bs-docs-sidenav
   [node-input lconn2]
   [simple-nodes lconn2]])



(defn find-by
  "Returns the unique entity identified by attr and val."
  [db attr val]
  (qe '[:find ?e
        :in $ ?attr ?val
        :where  [?e ?attr ?val]]
      db attr val))


(defn tx->id [tx]
  (select-one [:tempids sp/FIRST sp/LAST] tx))



(defn find-or-create-atom [db title]
  (or (:db/id (find-by @db :node/title title))
      (tx->id  (d/transact! db [{:db/id -1
                                 :node/title title
                                 :node/type :atom}]))))



(defn find-or-create-parent-child [db parent child]
  (or (d/q '[:find [?eid]
             :in $ ?parent ?child
             :where
             [?eid :set/parent ?parent]
             [?eid :set/child ?child]]
           @db
           parent
           child)
      (d/transact! db [{:db/id -1
                        :logic/type :subset
                        :set/parent parent
                        :set/child child}])))

(def relstring "Booky > Physics Booky > QED Books")

(defn sets-from-string [conn s]
 (let [units (str/split s " > ")
       eids (map (p find-or-create-atom conn) units)]
   (reduce (fn [parent child]
             (when parent
               (find-or-create-parent-child conn parent child))
             child
             ) nil  eids)))



#_(defn mdrop [conn choices model on-change]
  [rc/single-dropdown
   :choices choices
   :label-fn (comp (p label/deep-label conn) :db/id)
   :id-fn :db/id
   :group-fn (comp str :node/type)])


#_(defn simple-drop
  (let [nodes ])
  )


(defcard-rg equality-map
  [:div (label/deep-label lconn2 12)]
  )


