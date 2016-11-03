(ns undead.cards.poem
  (:require
   [goog.i18n.DateTimeFormat :as dtf]
   [posh.core :as posh :refer [posh!]]
   [reagent-table.core :as rt]
   [cljs-time.core :as time :refer [now]]
   [keybind.core :as keys]
   [cljs.pprint :refer [pprint]]
   [undead.cards.multi :as multi]
   [datascript.core :as d]
   [com.rpl.specter :as sp :refer [ALL MAP-VALS]]
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   [clojure.set :as set]
   [re-com.core :as rc])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [com.rpl.specter.macros :refer [select select-one defpathedfn providepath declarepath]]
   [undead.subs :refer [deftrack]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]) )





(def poem-schema {:set/subsets        {:db/valueType   :db.type/ref
                                        :db/cardinality :db.cardinality/many}
                  :set/attributes        {:db/valueType   :db.type/ref
                                      :db/cardinality :db.cardinality/many}
                  :set/members          {:db/valueType   :db.type/ref
                                    :db/cardinality :db.cardinality/many}})

#_(defcard-rg tabletester
  (fn []
    [rt/reagent-table (r/atom {:headers ["Row 1" "Row 2" "Row 5" "Row 3" "Col 4"]
                               
                               :rows [[1 1 1 1 "D"]
                                      [2 20 2 2 "B"]
                                      [30 3 33 3 "C"]
                                      [3 3 3 0 "A"]
                                      ]}


                              )
     {:rows-selection [:ul {:li {:class "btnn"}}]}

     ]))



(def poemdb
  [{:db/id -1
    :set/title "Two Giant Fat People"
    :set/text "God and I have become
Like two giant fat people
Living in a tiny boat.
We keep
Bumping into each other
And laughing."
    }
   
   {:db/id -2
    :set/title "Don't break stuff"}
   {:db/id -3
    :set/title "Culture"
    :set/subsets [{:db/id -4
                   :set/title "Middle Eastern"
                   :set/subsets [{:db/id -5
                                  :set/title "Persian"
                                  :set/members [-1]}]}
                   {:db/id -6
                    :set/title "Islamic"
                    :set/subsets [{:db/id -7
                                   :set/title "Sufi"
                                   :set/members [-1]}]}]}
   {:db/id -8
    :set/title "Author"
    :set/subsets [{:set/title "Poet"
                   :set/subsets [-9]}]}
   {:db/id -9
    :set/title "Hafiz"
    :set/members [-1 -14]}

   {:db/id -10
    :set/title "Theme"
    :set/subsets [-11]}
   {:db/id -11
    :set/title "The Divine"
    :set/members [-1]}

   {:db/id -12
    :set/title "Type"
    :set/subsets [-13]}
   {:db/id -13
    :set/title "Poem"
    :set/attributes [-10 -8 -3]
    :set/members [-1 -14]
    }

   {:db/id -14
    :set/title "The School of Truth"
    :set/text "O fool, do something, so you won't just stand there looking dumb.
If you are not traveling and on the road, how can you call yourself a guide?

In the School of Truth, one sits at the feet of the Master of Love.
So listen, son, so that one day you may be an old father, too!

All this eating and sleeping has made you ignorant and fat;
By denying yourself food and sleep, you may still have a chance.

Know this: If God should shine His lovelight on your heart,
I promise you'll shine brighter than a dozen suns.

And I say: wash the tarnished copper of your life from your hands;
To be Love's alchemist, you should be working with gold.

Don't sit there thinking; go out and immerse yourself in God's sea.
Having only one hair wet with water will not put knowledge in that head.

For those who see only God, their vision
Is pure, and not a doubt remains.

Even if our world is turned upside down and blown over by the wind,
If you are doubtless, you won't lose a thing.

O Hafiz, if it is union with the Beloved that you seek,
Be the dust at the Wise One's door, and speak!" }])




(def poem-db (d/db-with (d/empty-db poem-schema)
                        poemdb))

(def attr-query '[:find [(pull ?attributes
                               [:set/title :set/members
                                {:set/subsets ...}]) ...]
                  :in $ ?type
                  :where [?e :set/title ?type]
                  [?e :set/attributes ?attributes]
                  ])


(def subset-rule
  '[[(member ?p ?e)
     [?p :set/members ?e]]
    [(member ?p ?e)
     [?p :set/subsets ?p1]
     (member ?p1 ?e)]
    ])

(deftest poem-queries
  (testing "the attributes of a poem"
    (is (= [{:set/title "Culture",
             :set/subsets
             [{:set/title "Middle Eastern",
               :set/subsets
               [{:set/title "Persian", :set/members [{:db/id 1}]}]}
              {:set/title "Islamic",
               :set/subsets
               [{:set/title "Sufi", :set/members [{:db/id 1}]}]}]}
            {:set/title "Author",
             :set/subsets
             [{:set/title "Poet", :set/members [{:db/id 10}]}]}
            {:set/title "Theme",
             :set/members [{:db/id 13}],
             :set/subsets [{:set/title "The Divine"
                            :set/members [{:db/id 1}]}]}]
           (d/q
            attr-query
            poem-db
            "Poem")))

    (is (= ["Two Giant Fat People""The School of Truth"] (d/q '[:find [?poems ...]
                                                                :in $ % ?parent
                                                                :where
                                                                [?p :set/title ?parent]
                                                                (member ?p ?e)
                                                                [?e :set/title ?poems]
                                                                ]
                                                              poem-db
                                                              subset-rule
                                                              "Hafiz")))
    (is (= ["Two Giant Fat People"] (d/q '[:find [?poems ...]
                                           :in $ % ?parent
                                           :where
                                           [?p :set/title ?parent]
                                           (member ?p ?e)
                                           [?e :set/title ?poems]
                                           ]
                                         poem-db
                                         subset-rule
                                         "Islamic")))
    (is (= ["Two Giant Fat People"] (d/q '[:find [?poems ...]
                                         :in $ % ?parent
                                         :where
                                         [?p :set/title ?parent]
                                         (member ?p ?e)
                                         [?e :set/title ?poems]
                                         ]
                                       poem-db
                                       subset-rule
                                       "Culture")))
    ))










(defn demo-filter [{:keys [set/title set/subsets set/members] :as n} ]
  (let [my-atom (r/atom false)]
    (fn []
      (let [member-count (count members)]
        [:div
         (if (< 0 member-count)
           [rc/h-box
            :gap "15px"
            :children [[rc/checkbox
                        :label title
                        :model my-atom
                        :on-change #(reset! my-atom %)]
                       [:span member-count]
                       ]]
           [:label title])
         [:div.indent
          (for [[x s] (map-indexed vector subsets)]
            ^{:key (str title x)}
            [demo-filter s])]]))))





(defcard-rg dfilter
  [demo-filter {:set/title "Culture",
                :set/subsets
                [{:set/title "Middle Eastern",
                  :set/subsets
                  [{:set/title "Persian", :set/members [{:db/id 1}]}]}
                 {:set/title "Islamic",
                  :set/subsets
                  [{:set/title "Sufi", :set/members [{:db/id 1}]}]}]}]
  )



(defcard-rg dfilter22
  (let [attrs (d/q attr-query poem-db "Poem")]
    [rc/h-box     :width "500px"

     :gap "1em"
     :children (vec
                (for [{:keys [set/title set/members set/subsets]} attrs]
                  ^{:key title}
                  [:div.left-bblack
                   [:h1 title]
                   (for [a subsets]
                     ^{:key a}[demo-filter a])]))]))





(def poemdb2
  (vec (concat poemdb
               [{:db/id -15
                 :set/title "Conor's Poem"
                 :set/text "Ay we go, eyn, do, trey, caher"
                 :set/_members[{:db/id -16
                                :set/title "Irish"
                                :set/_subsets
                                [{:set/title "European"
                                  :set/_subsets [-3]
                                  }] }
                               -13]}]
              )))


(def poem-conn (d/conn-from-db (d/db-with (d/empty-db poem-schema)
                                          poemdb2)))

(posh! poem-conn)

(deftest poem-query
  (let [member-title-query '[:find [?poems ...] 
                             :in $ % [?parent ...]
                             :where
                             [?p :set/title ?parent]
                             (member ?p ?e)
                             [?e :set/title ?poems]
                             ]]
    (testing "Only European Poems"
      (is (= ["Conor's Poem"]
             (d/q member-title-query
                  @poem-conn
                  subset-rule
                  ["European"])
             )
          ))
    (testing "European and Sufi Poems"
      (is (= #{ "Conor's Poem" "Two Giant Fat People" }
             (set (d/q member-title-query
                       @poem-conn
                       subset-rule
                       ["European" "Sufi"]))
             )
          ))
    (testing "All Poems"
      (is (= [1 10 15]
             (d/q '[:find [?e ...]
                   :in $ % [?parent ...]
                  :where
                   [?p :set/title ?parent]
                   (member ?p ?e)
                   ]
                  @poem-conn
                  subset-rule
                  ["Poem"])
             )
          ))))





(deftrack matching-or
  [conn matches]

  "takes n eids and returns a track with set of members, including in subsets"

  (set @(posh/q conn '[:find [?e ...]
                       :in $ % [?p ...]
                       :where (member ?p ?e)]
                subset-rule
                matches)))


(deftrack intersection [& xs]
  (apply set/intersection (map deref xs)))


(deftest applying-filters
  (testing "Conn intersections"
    (is (= #{3 4} @(intersection (r/atom #{3 4 5}) (r/atom #{2 3 4})))))
  (testing "get all the poems"
    (is (= #{1 10 15}
           @(matching-or poem-conn [13]))))
  (testing "get the Irish poem"
    (is (= #{15}
           @(intersection
             (matching-or poem-conn [13])
             (matching-or poem-conn [16])
             )
           ))
    )
  )


(deftrack query-from-state [db]
  (let [qs (:queries db)
        active-qs (filter (comp #(< 0 %) count :selected) qs)
        conn (:conn db)]
    @(apply intersection
      (for [a (map :selected active-qs)]
        (matching-or conn a)))))



(deftest applying-filters-via-state
  (let [state {:conn poem-conn
               :queries [{:id 5
                          :selected [13]}
                         {:id 6
                          :selected []}
                         {:id 7
                          :selected [16]}]}] 

    (testing
        (is 
         (= [{:id 5
              :selected [13]}
             {:id 7
              :selected [16]}]
            (filter (comp #(< 0 %) count :selected) (:queries state))
            )))
    (testing "get the Irish poem"
      (is (= #{15}
             @(query-from-state state)
             @(intersection
               (matching-or poem-conn [13])
               (matching-or poem-conn [16])
               )
             ))
      ))



  )






(defn filter-view [{:keys [set/title set/subsets set/members] :as n} ]
  (let [my-atom (r/atom false)]
    (fn []
      (let [member-count (count members)]
        [:div
         (if (< 0 member-count)
           [rc/h-box
            :gap "15px"
            :children [[rc/checkbox
                        :label title
                        :model my-atom
                        :on-change #(reset! my-atom %)]
                       [:span member-count]
                       ]]
           [:label title])
         [:div.indent
          (for [[x s] (map-indexed vector subsets)]
            ^{:key (str title x)}
            [demo-filter s])]]))))





(defcard-rg filter-view-card
  [filter-view {:set/title "Culture",
                :set/subsets
                [{:set/title "Middle Eastern",
                  :set/subsets
                  [{:set/title "Persian", :set/members [{:db/id 1}]}]}
                 {:set/title "Islamic",
                  :set/subsets
                  [{:set/title "Sufi", :set/members [{:db/id 1}]}]}]}]
  )



#_(def attr-query '[:find [(pull ?attributes
                               [:set/title :set/members
                                {:set/subsets ...}]) ...]
                  :in $ ?type
                  :where [?e :set/title ?type]
                  [?e :set/attributes ?attributes]
                  ])


(deftrack state-via-pull [conn attr]
  @(posh/q conn '[:find [?e ...]
                  :in $ ?type
                  :where [?e :set/title ?type]
                  [?e :set/attributes ?attributes]]
           attr))


(defonce app-state (r/atom {:queries {}}))



(defn filter-load [a conn db]
  (let [x (posh/pull conn '[:set/title :db/id
                            {:set/subsets ...
                             :set/members ...}] a)]
    (fn [a conn db]
      (let [t (:db/id @x)]
        [:div
         (pr-str @db)
         [:h1 (:set/title @x)]
         (doall (for [s (:set/subsets @x)
                      :let [sid (:db/id s)
                            included? (contains? (get-in @db [:queries t :selected]) sid)]]
                  ^{:key (str s)}[rc/h-box
                                  :gap "10px"
                                  :children [[:label (:set/title s)]
                                             [:label (pr-str included?)]
                                             [:button
                                              {:on-click
                                               #(do
                                                 (if included?
                                                   (swap! db update-in
                                                          [:queries t :selected] disj sid)
                                                   (swap! db update-in
                                                          [:queries t :selected] (fnil conj #{}) (:db/id s))))}
                                              (str included?)
                                              ]
                                             #_[:input
                                              {:type "checkbox"
                                               :checked included?
                                               :read-only true
                                               :on-click #(do
                                                            (if included?
                                                              (swap! db update-in
                                                                     [:queries t :selected] disj sid)
                                                              (swap! db update-in
                                                                     [:queries t :selected] (fnil conj #{}) (:db/id s))))}]]]
                  ))]

        ))))


(defn filter2 [conn db]
  (let [qattrs
        (posh/q conn '[:find [?attrs ...]
                       :in $ ?type
                       :where
                       [?e :set/title ?type]
                       [?e :set/attributes ?attrs]]
                "Poem")]
    (fn [db]
      [:div
       [:h1 (pr-str @qattrs)]
       (for [a @qattrs]
              ^{:key (pr-str a)}
              [filter-load a conn db]

              )])))



#_(defcard-rg fff
  [filter2 poem-conn app-state])


#_(defcard-rg ffffff
  (fn []
    [:div.flex
     [filter2 poem-conn app-state]
     [:div 
      (pr-str (query-from-state app-state))]]))












(def app-state2 (r/atom {:queries {2 {:id 2
                                     :selected #{5}}
                                  7 {:id 7
                                     :selected #{ 9 }} }}))

(defn filter-load2 [a conn db]
  (let [x (posh/pull conn '[:set/title :db/id
                            {:set/subsets ...
                             :set/members ...}] a)
        b (r/atom false)]
    (fn [a conn db]
      (let [t (:db/id @x)]
        [:div
         [:h1 (:set/title @x)]
         [:div.bblack
          ]

         (doall (for [s (:set/subsets @x)
                      :let [sid (:db/id s)
                            included? (contains? (get-in @db [:queries t :selected]) sid)]]
                  [rc/h-box
                   :children [[:label (:set/title s)]
                              [:input
                               {:type "checkbox"
                                :checked included?
                                :on-click #(do
                                             (if included?
                                               (swap! db update-in
                                                      [:queries t :selected] disj sid)
                                               (swap! db update-in
                                                      [:queries t :selected] (fnil conj #{}) (:db/id s))))}]]]
                  ))]

        ))))


(defn filter22 [conn db]
  (let [qattrs
        (posh/q conn '[:find [?attrs ...]
                       :in $ ?type
                       :where
                       [?e :set/title ?type]
                       [?e :set/attributes ?attrs]]
                "Poem")]
    (fn [conn db]
      [:div
       (for [a @qattrs]
              ^{:key (pr-str a)}
              [filter-load2 a conn db])]

      )
      )
  )



(deftrack query-from-state2 [conn db]
  (let [qs (:queries db)
        active-qs (filter (comp #(< 0 %) count :selected) qs)
        ]
    @(apply intersection
            (for [a (map :selected active-qs)]
              (matching-or conn a)))))


(deftest specter
  (testing "stuff"
    (is (= [] (select [sp/ATOM :queries ALL ALL (sp/must :selected) ] app-state)))))


#_(defcard-rg ffff2
  (fn []
    (let [filters (select [sp/ATOM :queries ALL ALL (sp/must :selected) (comp #(< 0 %) count )] app-state)
          poem-q (posh/q poem-conn '[:find [?e ...]
                                :in $ ?type
                                :where [?p :set/title ?type]
                                [?p :set/members ?e]]
                         "Poem")]
      [:div.flex
       [:div (pr-str
              (if (empty? filters)
                (set @poem-q)
                (apply set/intersection
                       (set @poem-q)
                       (map deref
                            (map
                             #(matching-or poem-conn %)
                             (select [sp/ATOM :queries ALL ALL (sp/must :selected) (comp #(< 0 %) count )] app-state))))))]
       [filter22 poem-conn app-state]])))




(declarepath TOPSORT)
(providepath TOPSORT
             (sp/stay-then-continue
              :set/subsets ALL TOPSORT))







(deftest pulling-deep-nodes
  (let [nodes [{:set/title "Culture",
                :set/subsets
                [{:set/title "Middle Eastern",
                  :set/subsets
                  [{:set/title "Persian", :set/members [{:db/id 1}]}]}
                 {:set/title "Islamic",
                  :set/subsets
                  [{:set/title "Sufi", :set/members [{:db/id 1}]}]}]}
               {:set/title "Author",
                :set/subsets
                [{:set/title "Poet", :set/members [{:db/id 10}]}]}
               {:set/title "Theme",
                :set/members [{:db/id 13}],
                :set/subsets [{:set/title "The Divine"
                               :set/members [{:db/id 1}]}]}]
        nodes2 (posh/pull poem-conn '[:set/title :db/id
                                      {:set/subsets ...
                                       :set/members ...}] 3)
        ]
    (testing "pulling nodes out"
      (is (= []
             (select [ALL TOPSORT :set/title] nodes)))

      (is (= (map :set/title (select [TOPSORT] @nodes2))
             (select [TOPSORT :set/title] @nodes2)))
      )
    
    )

  )

(defn filter-load3 [a conn db]
  (let [x (posh/pull conn '[:set/title :db/id
                            {:set/subsets ...
                             :set/members ...}] a)
        ]
    (fn [a conn db]
      (let [t (:db/id @x)
            subsets (select [:set/subsets ALL TOPSORT] @x)]
        [:div
         [:h1 (:set/title @x)]
         

         (doall (for [s subsets
                      :let [sid (:db/id s)
                            included? (contains? (get-in @db [:queries t :selected]) sid)]]
                  [rc/h-box
                   :children [[:label (:set/title s)]
                              [:input
                               {:type "checkbox"
                                :checked included?
                                :on-click #(do
                                             (if included?
                                               (swap! db update-in
                                                      [:queries t :selected] disj sid)
                                               (swap! db update-in
                                                      [:queries t :selected] (fnil conj #{}) (:db/id s))))}]]]
                  ))]

        ))))

(defn filter-3 [conn db]
  (let [qattrs
        (posh/q conn '[:find [?attrs ...]
                       :in $ ?type
                       :where
                       [?e :set/title ?type]
                       [?e :set/attributes ?attrs]]
                "Poem")]
    (fn [conn db]
      [:div
       (for [a @qattrs]
         ^{:key (pr-str a)}
         [filter-load3 a conn db])]

      )
    )
  )

(defn simple-poem [conn eid]
  (let [poem (posh/pull conn '[:set/title :set/text] eid)]
    (fn []
      [:div.poem
       [:h1 (:set/title @poem)]
       (:set/text @poem)])))


(defn poem [poem-conn app-state]
  (fn []
    (let [filters (select
                   [sp/ATOM
                    :queries
                    ALL
                    ALL
                    (sp/must :selected)
                    (comp #(< 0 %) count )]
                   app-state)
          poem-q (posh/q
                  poem-conn
                  '[:find [?e ...]
                    :in $ ?type
                    :where [?p :set/title ?type]
                    [?p :set/members ?e]]
                  "Poem")
          poem-results (if (empty? filters)
                         (set @poem-q)
                         (apply set/intersection
                                (set @poem-q)
                                (map deref
                                     (map
                                      #(matching-or poem-conn %)
                                      (select [sp/ATOM :queries ALL ALL (sp/must :selected) (comp #(< 0 %) count )] app-state)))))]
      [:div.flex
       [filter-3 poem-conn app-state]
       [:div (for [p  poem-results]
               ^{:key p}[simple-poem poem-conn p])]])))




(defn qe
  "Returns the single entity returned by a query."
  [query db & args]
  (when-let [result (-> (apply d/q query db args) ffirst)]
    (d/entity db result)))

(defn find-by
  "Returns the unique entity identified by attr and val."
  [db attr val]
  (qe '[:find ?e
        :in $ ?attr ?val
        :where  [?e ?attr ?val]]
      db attr val))




(defn subset-member-q
  "takes a eid, returns eids"
  [conn top]
  (posh/q conn '[:find [?e ...]
                 :in $ % ?topeid
                 :where (member ?topeid ?e)
                 ]
          subset-rule
          top))

(def subset-only-rule
  '[[(member ?p ?e)
     [?p :set/subsets ?e]]
    [(member ?p ?e)
     [?p :set/subsets ?p1]
     (member ?p1 ?e)]
    ])


(defn subset-q
  "takes a eid, returns eids"
  [conn top]
  (posh/q conn '[:find [?e ...]
                 :in $ % ?topeid
                 :where (member ?topeid ?e)
                 ]
          subset-only-rule
          top))

(defn get-attrs [conn attr eids]
  (posh/q conn '[:find ?a
                 :in $ ?attr [?eid ...]
                 :where [?eid ?attr ?a]]
          attr
          eids))


(deftest subsest-q-test
  (testing "poem-conn queries"
    (let [culture-stuff (subset-q
                         poem-conn
                         (:db/id
                          (find-by @poem-conn :set/title "Culture")))]

      (is (= "" @culture-stuff))
      (is (= "" (get-attrs poem-conn :set/title @culture-stuff))))))




(deftrack title-map [conn root]
  (apply hash-map @(posh/q conn '[:find [?title ?e]
                                      :in $ % ?parent
                                      :where [?p :set/title ?parent]
                                      (member ?p ?e)
                                      [?e :set/title ?title]]
                          subset-only-rule
                          root
                          )))

(def title-subset-title '[:find [?title ...]
                         :in $ % ?parent
                         :where [?p :set/title ?parent]
                         (member ?p ?e)
                         [?e :set/title ?title]])

(defn title-subset-q [conn root]
  (posh/q conn
          title-subset-title
          subset-only-rule
          root))

(defn transaction->eid [tx]
                  (-> tx
                      :tempids
                      first
                      second))

(deftest mapmaking
  (testing "turn vectors into map"
    (is (= {:a "b"} (apply hash-map [:a "b"])))
    )
  (testing "return from transaction"
    (is (integer? (transaction->eid (d/transact! poem-conn [{:db/id -1 :set/title "New Title"}]))))
  ))



(defn transact-root [conn v root]
  (let [theme-id (:db/id (find-by @conn :set/title root))]
    (d/transact! conn [{:db/id -1
                        :set/title v
                        :set/_subsets theme-id}])))


(defn subset-builder [conn map-atom selections root]
  (map (fn [v] (get @map-atom v
                   (transaction->eid
                    (transact-root
                     conn
                     v
                     root))
                   )) @selections))



(defn find-or-create-subset [conn root v]
  (or (d/q '[:find ?e .
             :in $ % ?title ?root
             :where
             [?e :set/title ?title]
             (member ?r ?e)
             [?r :set/title ?root]]
           @conn
           subset-only-rule
           v
           root)
      (transaction->eid
       (transact-root
        conn
        v
        root))))

(deftest find-or-create-test
  (testing "aaa"
    (is (= 17 (find-or-create-subset poem-conn "Culture" "Irish")))
    (is (= 21 (find-or-create-subset poem-conn "Culture" "Hindu"))))
  )


(defcard-rg culture-multisimple
  (let [theme-selections (r/atom [])
        theme-map (title-map poem-conn "Theme")
        theme-options (title-subset-q poem-conn "Theme")]
    (fn []
      [:div
       (pr-str (apply hash-map @theme-map))
       [rc/v-box
        :gap "10px"
        :children [
                   [multi/multi
                    {:highlight-class "highlight"
                     :options @theme-options
                     :placeholder "Theme"
                     :selections theme-selections
                     :on-delete #(swap! theme-selections pop)
                     :save! #(swap! theme-selections conj %)}]
                   [:button {:on-click #(js/alert
                                         (map (partial find-or-create-subset
                                                poem-conn
                                                "Theme")                                       @theme-selections))}
                    "test"]]]
       ])))




(defn multis [hidden]
  (let [poem-id (:db/id (find-by @poem-conn :set/title "Poem"))
        culture-selections (r/atom [])
        culture-options (title-subset-q poem-conn "Culture")
        theme-selections (r/atom [])
        theme-options (title-subset-q poem-conn  "Theme")
        author-selections (r/atom [])
        author-options (title-subset-q poem-conn "Author")
        text-body (r/atom "")
        text-title (r/atom "")]
    (fn [hidden]
      (if @hidden
        [:button {:on-click #(reset! hidden false)} "Show the modal"]
        [rc/modal-panel
         :backdrop-on-click #(reset! hidden true)
         :child [:div
                 [:input {:value @text-title
                          :placeholder "Poem Title"
                          :on-change #(reset! text-title (-> % .-target .-value))}]

                 [:textarea {:value @text-body
                             :on-change #(reset! text-body (-> % .-target .-value))
                             :placeholder "Poem Body"}]
                 [rc/v-box
                  :gap "10px"
                  :children [[multi/multi
                              {:highlight-class "highlight"
                               :options @culture-options
                               :placeholder "Culture"
                               :selections culture-selections
                               :on-delete #(swap! culture-selections pop)
                               :save! #(swap! culture-selections conj %)}]
                             [multi/multi
                              {:highlight-class "highlight"
                               :options @theme-options
                               :placeholder "Theme"
                               :selections theme-selections
                               :on-delete #(swap! theme-selections pop)
                               :save! #(swap! theme-selections conj %)} 
                              ]
                             [multi/multi
                              {:highlight-class "highlight"
                               :options @author-options
                               :placeholder "Author"
                               :selections author-selections
                               :on-delete #(swap! author-selections pop)
                               :save! #(swap! author-selections conj %)} 
                              ]]]
                 [:button {:on-click #(let
                                          [subsets (vec (concat
                                                        [poem-id]
                                                        (map (partial find-or-create-subset
                                                                poem-conn
                                                                "Theme")
                                                             @theme-selections)
                                                        (map (partial find-or-create-subset
                                                                poem-conn
                                                                "Culture")
                                                             @culture-selections)
                                                        (map (partial find-or-create-subset
                                                                poem-conn
                                                                "Author")
                                                             @author-selections)))]
                                        (do
                                          (d/transact! poem-conn
                                                       [{:db/id -1
                                                         :set/title @text-title
                                                         :set/text @text-body
                                                         :set/_members subsets
                                                         }]
                                                       )
                                          (reset! text-title "")
                                          (reset! text-body "")
                                          (reset! hidden true)))}
                  "Save Poem"]]]))))






(deftest concat-test
  (testing "aaaaaaaa"
    (let [poem-id (:db/id (find-by @poem-conn :set/title "Poem"))
          culture-selections (r/atom ["Irish"])
          culture-options (title-subset-q poem-conn "Culture")
          theme-selections (r/atom [])
          theme-options (title-subset-q poem-conn  "Theme")
          author-selections (r/atom [])
          author-options (title-subset-q poem-conn "Author")
          text-body (r/atom "")
          text-title (r/atom "")]
      (is (= []
             (vec (concat
                  [poem-id]
                  (map (partial find-or-create-subset
                          poem-conn
                          "Theme")
                       @theme-selections)
                  (map (partial find-or-create-subset
                          poem-conn
                          "Culture")
                       @culture-selections)
                  (map (partial find-or-create-subset
                          poem-conn
                          "Author")
                       @author-selections))))))
    ))


(let [poem-id (:db/id (find-by @poem-conn :set/title "Poem"))]
  (d/transact! poem-conn [{:db/id -1 :set/title "New Poem"
                           :set/text "heeeyyykj"
                           :set/_members [poem-id]}]))


(defcard-rg show-multis
  (let [hidden (r/atom false)]
    (fn []
      [:div
       [multis hidden]
       (when @hidden
         [poem poem-conn app-state])
       ]))
  )


(defn all-poems [conn]
  (posh/q conn '[:find [(pull ?e [*]) ...]
            :in $ %
            :where [?p :set/title "Poem"]
            (member ?p ?e)]
          subset-rule
          )
  )



(defcard-rg all-poems
  (let [es (all-poems poem-conn)]
    (fn []
      [:ol 
       (for [e @es]
         ^{:key e}[:li (pr-str e)])]))

  )

