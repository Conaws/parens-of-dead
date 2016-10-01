(ns undead.cards.grid
  (:require [cljs-time.core :refer [now]]
            [clojure.set :as set]
            [clojure.string :as str]
            [com.rpl.specter :as sp :refer [ALL]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
;            [undead.test :refer [title-parse]]
            [re-com.core :as rc :refer [v-box box h-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [conn e]]
            [clojure.string :as str])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack]]))

(def datatom (r/atom (now)))

(defonce newconn (d/create-conn subs/schema) )
(posh! newconn)

(def grid-transaction
  [{:db/id 1
    :node/type :set
    :node/title "Investors"
    :set/members [{:db/id -2
                   :node/title "Peter Thiel"}
                  {:db/id -3
                   :node/title "Elon Musk"}]}
   {:db/id -4
    :node/type :set
    :node/title "Contacted"
    :set/members [[:node/title "Peter Thiel"]
                  [:node/title "Elon Musk"]
                  ]}
   {:db/id -5
    :node/type :set
    :node/title "Scheduled"
    :set/members [[:node/title "Peter Thiel"]

                  ]}
   {:db/id -6
    :node/type :set
    :node/title "Investor Sets"
    :set/members [-5 -4 -7]
    }
   {:db/id -7
    :node/type :set
    :node/title "Pitched"
    :set/members [
                  ]}
   {:db/id -8
    :node/type :set
    :node/title "Super Set"
    :set/members [-6
                  ]}])


(d/transact! newconn grid-transaction)




(defn x-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))]
    (fn [props]
      [:input (merge props
                     {:type "text"
                      :value @val
                      :auto-focus true
                      :on-blur save
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))
(defn addset [conn title ptitle]
  (d/transact! conn [{:db/id -1
                      :node/title title
                      :node/type :set
                      :set/_members [:node/title ptitle]}])
  )


;; (defn editable-th [conn node]
;;   (let [editing (r/atom false)
;;         title (:node/title node)]
;;     (fn []
;;       (if @editing
;;         [:th [x-input {:title title
;;                        :on-stop #(reset! editing false)
;;                        :on-save #(d/transact! conn
;;                                               [[:db/add (:db/id node)
;;                                                 :node/title %]]
;;                                               )}]]
;;         [:th
;;          {:on-double-click #(reset! editing true)}
;;          title]
;;         )
;;       )
;;     )

;;   )


(declare add-elem-form)

(defn simple-table [conn xtitle ytitle]
  (let [xelems (posh/pull conn '[:node/title {:set/members ...}] [:node/title xtitle])
        ysets (posh/pull conn '[:node/title :node/type {:set/members ...}] [:node/title ytitle])
        newelem (r/atom false)
        newst (r/atom false)]
    (fn []
      [:div.gridtest
       [:table
        [:thead [:tr [:th xtitle][:th]
                 (doall (for [m (:set/members @ysets)]
                   ^{:key (str "th" m)} [:th (:node/title m)]))
                 [:th

                  [add-elem-form conn ytitle]

                  ]]]
        [:tbody
         (for [m (:set/members @xelems)]
          ^{:key (str m "table-title")} [:tr [:th (:node/title m)][:th]
            (doall (for [s (:set/members @ysets)]
               ^{:key (str s m)}(if (contains? (set (:set/members s)) m )
                 [:td.active
                  {:on-click
                   #(d/transact! conn [[:db/retract (:db/id s) :set/members (:db/id m) ]])

                   }"Y"]
                 [:td
                  {:on-click
                   #(d/transact! conn [{:db/id (:db/id s) :set/members (:db/id m)}])
                   }"N"])))])
         [:tr [:td (if @newelem [x-input
                                 {:title ""
                                  :on-stop #(reset! newelem false)
                                  :on-save #(do
                                              (d/transact! conn [{:db/id -1
                                                                  :node/title %
                                                                  :set/_members [:node/title xtitle]}])
                                              )}
                                 ]
                       [rc/button :label (str "New " xtitle )
                        :on-click #(reset! newelem true)])]
          [:td]]
         ]]
       ;; (pr-str @conn)
       ])))


(defn simple-sets [conn topgroup]
  (let [root (posh/pull conn '[:node/type :node/title {:set/members ...}] [:node/title topgroup])]
    (fn []
      [:div.nest
       [:ol
        (for [m (:set/members @root)]
         ^{:key (str m "list")} [:div
           [:label (:node/title m)]
           [:ol
            (for [m (:set/members m)]
            ^{:key (str m "label")}  [:li
               [:label {:draggable true}
                (:node/title m)]
               ])]])]
       ]
      )))

(defn testa []
  (fn []
    [rc/h-box
     :children [


                [v-box
                 :gap "10px"
                 :children
                 [

                  ;; [rc/datepicker-dropdown :model datatom
                  ;;  :on-change #(reset! datatom %)]
                  [rc/datepicker :model datatom
                   :on-change #(reset! datatom %)]
                  [rc/title :label "Investor Groupings"
                   :level :level1
                   :underline? true]
                  ;; [button :label (pr-str @newconn)]
                  [simple-table newconn "Investors" "Investor Sets"]

                  [simple-sets newconn "Investor Sets"]
                  ]
                 ]]]))
(defcard-rg tabledrawa
  [testa]
  )


(defn intersection-node [conn x y]
  (let [inter (posh/q conn '[:find [?title ...]
                             :in $ ?x ?y
                             :where [?x :set/members ?e]
                             [?y :set/members ?e]
                             [?e :node/title ?title]]
                      (e x)
                      (e y))]
    (fn []
      [:td (pr-str @inter)]     ;; (if (= x y)
      ;; [:td.active "\\"]
      ;; )
      )))

(defn add-elem-form [conn parent-title ]
  (let [newelem (r/atom false)]
    (fn []
      (if @newelem [x-input
                    {:title ""
                     :on-stop #(reset! newelem false)
                     :on-save #(do
                                 (d/transact! conn [{:db/id -1
                                                     :node/title %
                                                     :set/_members [:node/title parent-title]}])
                                 )}
                    ]
          [:button.btn.btn-default
           {:on-click #(reset! newelem true)}
           (str "New " parent-title )
           ]))))


(defn intersection-table [conn xtitle ytitle]
  (let [xsets (posh/pull conn '[:node/title :node/type {:set/members ...}] [:node/title xtitle])
        ysets (posh/pull conn '[:node/title :node/type {:set/members ...}] [:node/title ytitle])
        newelem (r/atom false)
        newst (r/atom false)]
    (fn []
      [:div

       [:div.gridtest
        [rc/title :label "Intersection"
         :level :level1
         :underline? true
         ]
        [:table
         [:thead
          [:tr [:th][:th] [:th.underline {:col-span "5"} ytitle] ]
          [:tr [:th.underline xtitle][:th]
           (doall (for [m (:set/members @ysets)]
              ^{:key (str "title" m)}[:th (:node/title m)]))
           [:th
            [add-elem-form conn ytitle ]
            ]]]
         [:tbody
          (doall (for [m (:set/members @xsets)]
           ^{:key (str m "rows")}  [:tr [:th (:node/title m)][:th]
                                    (doall (for [s (:set/members @ysets)]
                                            ;; (if (contains? (set (:set/members s)) m )
                                            ;;   [:td.active
                                            ;;    {:on-click
                                            ;;     #(d/transact! conn [[:db/retract (:db/id s) :set/members (:db/id m) ]])
                                            ;;     }"Y"]
                                            ;;   [:td
                                            ;;    {:on-click
                                            ;;     #(d/transact! conn [{:db/id (:db/id s) :set/members (:db/id m)}])
                                            ;;     }"N"]))])
                                            ^{:key (str m s "inters")}    [intersection-node conn m s]
                                            ))]))
          [:tr [:td (if @newelem [x-input
                                  {:title ""
                                   :on-stop #(reset! newelem false)
                                   :on-save #(do
                                               (d/transact! conn [{:db/id -1
                                                                   :node/title %
                                                                   :set/_members [:node/title xtitle]}])
                                               )}
                                  ]
                        [rc/button :label (str "New " xtitle )
                         :on-click #(reset! newelem true)])]
           [:td]]
          ]]
        ;; (pr-str @conn)
        ]])))




(defcard-rg intertable
  [intersection-table newconn "Investor Sets" "Investor Sets"])



(defn interdrop [conn m s]
  (let [inner-sets (:set/members s)
        matching-inner (->> inner-sets
                            (select [ALL (sp/selected? [:set/members #(-> %
                                                                          set
                                                                          (contains? m))])
                                     :node/title])) ]
    [:td
     (pr-str matching-inner)
     #_[rc/selection-list
        :choices (r/atom (vec (range 10)))
        :model (r/atom (vec (range 10)))
        :on-change #(js/alert %)]
     ]
    ;; [:td (pr-str choices)]
    ))



(defn folding-table [conn xtitle ytitle]
  (let [xsets (subs/by-title conn xtitle)
        ysets (subs/by-title conn ytitle)
        newelem (r/atom false)
        newst (r/atom false)]
    (fn []
      [:div
       [:div.gridtest
        [rc/title :label "Nested Sets"
         :level :level1
         :underline? true
         ]
        [:table
         [:thead
          [:tr [:th][:th] [:th.underline {:col-span "5"} ytitle] ]
          [:tr [:th.underline xtitle][:th]
           (for [m (:set/members @ysets)]
            ^{:key (str "folding" m)} [:th (:node/title m)])
           [:th

            (if @newst
              [x-input {:title ""
                        :on-stop #(reset! newst false)
                        :on-save #(addset conn % ytitle)
                        }]
              [rc/button :class "btn" :label (str "New " ytitle)
               :on-click #(reset! newst true)]
              )

            ]]]
         [:tbody
          (for [m (:set/members @xsets)]
            ^{:key (str "folding-header" m)}[:tr [:th (:node/title m)][:th]
             (for [s (:set/members @ysets)]
               ^{:key (str "folding-header" m)}
               [interdrop conn m s]
               )])
          [:tr [:td (if @newelem [x-input
                                  {:title ""
                                   :on-stop #(reset! newelem false)
                                   :on-save #(do
                                               (d/transact! conn [{:db/id -1
                                                                   :node/title %
                                                                   :set/_members [:node/title xtitle]}])
                                               )}
                                  ]
                        [rc/button :label (str "New " xtitle )
                         :on-click #(reset! newelem true)])]
           [:td]]
          ]]
        ;; (pr-str @conn)
        ]])))


(defcard-rg folding
  [folding-table newconn "Investors" "Super Set"])


;; (defn selections [choices starting-selections]
;;   (let [
;;         selection-id (r/atom (set starting-selections))]
;;     (fn []
;;       [rc/selection-list
;;        :choices sorted-nodes
;;        :model selection-id
;;        :on-change #(reset! selection-id %)]
;;                ]))


;; (defcard-rg selecttest2
;;   [select-node2])


(def currently-dragging (r/atom {}))

(defn add-elem-form2 [conn parent-title ]
  (let [newelem (r/atom false)]
    (fn []
      (if @newelem [x-input
                    {:title ""
                     :on-stop #(reset! newelem false)
                     :on-save #(do
                                 (d/transact! conn [{:db/id -1
                                                     :node/title %
                                                     :set/_members [:node/title parent-title]}])
                                 )}
                    ]
          [:subs
           {:on-click #(reset! newelem true)}
           "+"]))))



(defn allow-drop [e]
  (.preventDefault e))

(defn add-to-set [conn parent-title child-title]
  (d/transact! conn [{:db/id [:node/title parent-title]
                      :set/members [:node/title child-title]}]))

(defn drop-add [conn currently-dragging]
  (let [{:keys [dragging target]} @currently-dragging]
    (if (not= dragging target)
      (add-to-set conn target dragging))))

(defn simple-folding-sets [conn topgroup open]
  (let [root (posh/pull conn '[:node/type :node/title {:set/members ...}] [:node/title topgroup])
        open (r/atom open)
        hovered (r/atom false)]
    (fn [conn topgroup]
      [:div.nest
       {:on-drag-enter #(do
                          (allow-drop %)
                          (reset! hovered true))
        :on-drag-over allow-drop
        :on-drag-exit #(reset! hovered false)
        :on-mouse-leave #(reset! hovered false)}

       [:div.flex
        [:button.left-bar
         {:on-click #(swap! open not)}]
        [:label
         {:draggable true
          :on-drag-start #(swap! currently-dragging assoc :dragging topgroup)}
         (:node/title @root)]
        (pr-str @hovered)
        (when @open
          [add-elem-form2 conn (:node/title @root)])]
       (when @open
         [:div
          [:ol
           (for [m (:set/members @root)]
             ^{:key (str m "simple-folding-sets")}[simple-folding-sets conn (:node/title m) open])

           (if @hovered
             [:div.dropzone
              {:on-drag-enter #(do
                                 (allow-drop %)
                                 (swap! currently-dragging assoc :target topgroup))
               :on-drag-over allow-drop
               :on-drop #(drop-add conn currently-dragging)
               }
              (if-let [{:keys [dragging target] :as e } @currently-dragging]
                (if (= topgroup target)
                  (pr-str e))
  )
              ]
             [:label "drop here"]
             )


           ]])
       ]
      )))




(defcard-rg fold
  [:div.flex.nest
   {:style {:justify-content "space-between"}}
   [simple-folding-sets newconn "Investors" true]
   [simple-folding-sets newconn "Investor Sets" true]]
  )



(defcard-rg lint
  [:ol#bso
   [:li.linkz>div.linkz "aaaa aaa"]
   [:li>a.linkz"aaaa aaa"]])

(defcard-rg navy
  [:div#bso
   [:ul.flex.nav
    [:li [:a "A"]
     [:div.nav__dropdown>div.megadropdown

"Cras placerat accumsan nulla.  Nulla facilisis, risus a rhoncus fermentum, tellus tellus lacinia purus, et dictum nunc justo sit amet elit.  "

      ]]
    [:li [:a "B"]]
    [:li [:a "C"]]]]
  )



(defn show [{:keys [img title] :as s}]
  [:div
   [:a
    [:img {:src img}]
    [:h3 title]]
   ])

(defn shows [shows]
  [:div#bso
   [:div.box
    [:h1 "New"]
    [:div.box-body
     (for [s shows]
       ^{:key (str "shows " s)}[show s])
     ]
    [:div.box-footer>a "More New Episodes"]]

   [:div.layout-content>div.box
    
    ]
   ]

  )

#_(defcard-rg showtest
  [shows [{:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}
          {:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}
          {:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}
          {:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}
          {:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}
          {:img "https://images.bewakoof.com/utter/content-rich-movies-9-low-budget-films-that-didnt-receive-the-spotlight.jpg"
           :title "film"}]



   ]
  )


(defn count-tabs
  [string]
  (count (take-while #{\tab} string)))




(defn parsed [text]
  (->> (str/split text #"\n")
       (filter (comp not str/blank?))
       (map (juxt count-tabs str/trim))))

(defn parsed-with-index [text]
  (->> (str/split text #"\n")
       (map-indexed (juxt (fn [i x] (count-tabs x))
                          (fn [i x] [i (str/trim x)])))))






(defn transform-depthvec [nodefn edgefn sibling-collector nseq]
  (loop [result []
         s nseq]
    (let[[pdepth ptitle] (first s)
         [children siblings] (split-with #(< pdepth (first %)) (rest s))
         answer   (nodefn ptitle)
         answer
         (if (seq children)
           (edgefn answer (transform-depthvec nodefn edgefn sibling-collector children))
           answer)]
      (if (seq siblings)
        (recur (sibling-collector result answer) siblings)
        (sibling-collector result answer)))))



(defn title-parse [matcher title]
  (if (and (string? title) (str/starts-with? title matcher))
    (let [newtitle (str/replace-first title matcher "")]
      (str/trim newtitle))   )  )

(defn create-node-map [title]
  (or (when-let [t (title-parse "<" title)]
        {:input/type :parent
         :node/title t})
      (when-let [t (title-parse "+" title)]
        {:input/type :intersection
         :node/type :intersection
         :set/parents (vector t)})
      {:input/type :child
       :node/title title}))


;; this works fine IF, I can have deeply nested lookup,
;; and IF, it isn't too hard to remove the items
;; I have a feeling it won't work the way I want it to though


;;; realized why I can't pass the path down in intersection nodes-- because this is a recursive fn
;;; thus it moves from bottom up, not top down, the bottom ones don't even have
;;; anything they COULD add.. 

(defn connect-node [{title :node/title
                     type :input/type
                     parents :set/parents
                     :as node} children]
  (let [{c :child p :parent i :intersection} (group-by :input/type children)]
    (cond-> node
      c (assoc :set/members c)
      p (assoc :set/_members p)
      i (assoc :set/intersections
               (condp = type
                 :child (mapv #(update % :set/parents
                                       (fn [x] (conj x title))) i)
                 :parent (mapv #(update % :set/parents
                                        (fn [x] (conj x title))) i)
                 :intersection (mapv #(update % :set/parents (fn [x] (concat parents x))) i))
               ))))



(def sample-intersection
  [{:db/id -1
    :node/title "Books"}
   {:db/id -2
    :node/type :intersection
    :set/parents ["Books" {:node/title "Philosophy"}]
    :set/members [{:node/title "Principia Mathematica"}]}
   ]

  )







(def depthvec->tree
  (partial transform-depthvec create-node-map connect-node conj))

(defonce teststring (r/atom {:text "A"}))



(deftrack tracktest [db]
  (depthvec->tree (parsed (:text @db))))




(defn event-handler [db [event-name & event-vec]]
  (js/console.log (pr-str  db event-name event-vec))
  (case event-name
    :change-text
    (let [[t] event-vec
          t (or t "")]
      (assoc db :text t))
    :tab-down
    (let [text (:text db)
          [start end] event-vec
          newstring (str (subs text 0 start) "\t"  (subs text end))]
      (js/console.log (pr-str newstring))
        (assoc db :text (str (subs text 0 start) "\t"  (subs text end))))
    ))


(defn string-between [between s]
  (apply str (rest (interleave (repeat between) s))))




(defn emit [e]
  (do (js/console.log "handle event" (pr-str e))
      (r/rswap! teststring event-handler e)))


(defn handle-tab-down [e]
  (case (.-which e)
    9 (do
        (emit [:tab-down
                   (-> e .-target .-selectionStart)
               (-> e .-target .-selectionEnd)])
        (.preventDefault e))
    :else))


(declare node-test)
(defn inter [conn {parents :set/parents
                   children :set/members
                   i :set/intersections
                   :as node}]
  [:div
   [:button.btn.btn-intersection
        (string-between " + " parents)]
   [:div.tree-children
    (for [c children]
      ^{:key (str c node)}
      [node-test conn c])
    ]
   (when i
     [:div.tree-children
      (for [p i]
        ^{:key (str i p)}

        [inter conn p])

      ])]
  )


(defn node-test [conn {title :node/title
                       members :set/members
                       parents :set/_members
                       inters :set/intersections
                       :as node}]
  (let [existing @(posh/q conn '[:find ?e
                                  :in $ ?title
                                  :where
                                  [?e :node/title ?title]]
                            title)]
    [:div.tree
     [:div.tree-node
      [:button.circle]
      (if-let [ex (ffirst existing)]
        [:button.btn title]
        [:b title])]
     [:div.tree-children
      (for [m members]
       ^{:key (str title m)} [node-test conn m])
      (when parents
        [:div.tree-children.tree-children-parents
         (for [p parents]
           ^{:key (str title p)}[node-test conn p])])

      (when inters
        [:div.tree-children
         (for [p inters]
           ^{:key (str title p)}
           [inter conn p])

         ])
      ]
     ]))


(defonce parsed-string (tracktest teststring))


(defn tree-view [conn db]
  (let [tree @(tracktest db)]
    [:div (for [node tree]
                ^{:key (str "treeview" node)}[node-test conn node])]))


(defn tree-input [conn db]
  [:div#bso
   [:div.layout-split.flex
    [:button.btn {:on-click
                  #(d/transact! conn @(tracktest db))}]
    [:textarea {:value (:text @db)
                :on-change #(emit [:change-text (-> % .-target .-value)])
                :on-key-down handle-tab-down}]
    [tree-view conn db]]
    [:div.well.bblack (pr-str @(tracktest db))]
    [:div.well (pr-str @conn)]]
  )



(defcard-rg looka
 [tree-input newconn teststring] 
  teststring
  {:inspect-data true
   :history true})







(defn db-display []
  [:div
   [:button {:on-click 
             #(d/transact! newconn @parsed-string)}]
   (pr-str @newconn)
   ]
  )



(defcard-rg testtransact
  [db-display])

(defn node-tes []
  [:label.node

   [:div.node-id
    [:div 45]]

   "Just a quick visualization"

   [:span.node-parents 4]
   [:span.node-children 2]

   ]
)







(defcard-rg sticky-list
  [:div
   [:dl
    [:dt "A"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]]
   [:dl
    [:dt "b"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]
    [:dd "a termm"]]
    [:dl
     [:dt "d"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]]
    [:dl
     [:dt "c"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     [:dd "a termm"]
     ]]
  )

(defcard-rg nodecss
  [node-tes])
(defcard-rg testtransact2
  [:div
   [:button {:on-click 
             #(d/transact! newconn @parsed-string)}]
   (pr-str @newconn)
   ]

  )

(def complexparent-test "Investor Sets\n\tGreat Guys\nInvestors\n\tE Pluribus\n\t\t< Great Guys\n\t\t\tElon Musk")

(def more-complex 
  "Investor Sets\n\tGreat People\n\t\tEmmit Smith\nBennet Smith\n\t< Awesome Folks\n\t
  t< Investor Sets\nWill Smith\n\t< Fantastic People\n\t\t< Investor Sets")



(deftest test-resolve-current-tx
  (let [conn (d/create-conn {:created-at {:db/valueType :db.type/ref}})
        tx1  (d/transact! conn [{:name "X"
                                 :created-at :db/current-tx}
                                {:db/id :db/current-tx
                                 :prop1 "prop1"}
                                [:db/add :db/current-tx :prop2 "prop2"]
                                [:db/add -1 :name "Y"]
                                [:db/add -1 :created-at :db/current-tx]])]
    (is (= (d/q '[:find ?e ?a ?v :where [?e ?a ?v]] @conn)
           #{[1 :name "X"]
             [1 :created-at (+ d/tx0 1)]
             [(+ d/tx0 1) :prop1 "prop1"]
             [(+ d/tx0 1) :prop2 "prop2"]
             [2 :name "Y"]
             [2 :created-at (+ d/tx0 1)]}))
    (is (= (:tempids tx1) {-1 2, :db/current-tx (+ d/tx0 1)}))
    (let [tx2   (d/transact! conn [[:db/add :db/current-tx :prop3 "prop3"]])
          tx-id (get-in tx2 [:tempids :db/current-tx])]
      (is (= tx-id (+ d/tx0 2)))
      (is (= (into {} (d/entity @conn tx-id))
             {:prop3 "prop3"})))))





(deftest test-resolve-eid-refs
  (let [conn (d/create-conn {:friend {:db/valueType :db.type/ref
                                      :db/cardinality :db.cardinality/many}})
        tx   (d/transact! conn [{:name "Sergey"
                                 :friend [-1 -2]}
                                [:db/add -1 :name "Ivan"]
                                [:db/add -2 :name "Petr"]
                                [:db/add -4 :name "Boris"]
                                [:db/add -4 :friend -3]
                                [:db/add -3 :name "Oleg"]
                                [:db/add -3 :friend -4]])
        q '[:find ?fn
            :in $ ?n
            :where [?e :name ?n]
                   [?e :friend ?fe]
                   [?fe :name ?fn]]]
    (is (= (:tempids tx) { -1 2, -2 3, -4 4, -3 5, :db/current-tx (+ d/tx0 1) }))
    (is (= (d/q q @conn "Sergey") #{["Ivan"] ["Petr"]}))
    (is (= (d/q q @conn "Boris") #{["Oleg"]}))
    (is (= (d/q q @conn "Oleg") #{["Boris"]}))))





