(ns undead.cards.search
  (:require [cljs-time.core :refer [now]]
            [clojure.set :as set]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [v-box box h-box]]
            [reagent.core :as r]
            [clojure.string :as str]
            [devcards.core :as dc])
  (:require-macros
   [cljs.test  :refer [testing is are]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack multi-filter mtest]]))


(def test-db (d/db-with (d/empty-db {:node/title {:db/unique :db.unique/identity}
                                     :set/down {:db/valueType :db.type/ref
                                                   :db/cardinality :db.cardinality/many}
                                     :set/up  {:db/valueType :db.type/ref
                                                         :db/cardinality :db.cardinality/many}
                                     :set/intersections  {:db/valueType :db.type/ref
                                                  :db/cardinality :db.cardinality/many}
                                     :intersection/of  {:db/valueType :db.type/ref
                                                   :db/cardinality :db.cardinality/many}})
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
                         {:db/id 7
                          :node/title "Doug's Favorite Books"
                          :intersection/of #{4 5 1}}
                         {:db/id 8
                          :node/title "Memex"
                          :set/up [3 4]}
                         {:db/id 9
                          :node/title "Conor"}
                         {:db/id 10
                          :node/title "Conor's Favorite Lists"
                          :set/up [9]
                          :set/down [7]}
                         {:db/id 11
                          :node/title "As we may think"
                          :set/up #{3 7}
                          :set/down #{8}}
                         ]))


(defonce conn (d/conn-from-db test-db))
(posh! conn)

(def child-rule
  '[[(child ?e ?e2)
     [?e :set/down ?e2]]
    [(child ?e ?e2)
     [?e2 :set/up ?e]]]
  )


(def intersection-rule
  '[[(intersection ?e ?i)
     [?e :set/intersections ?i]]
    [(intersection ?e ?i)
     [?i :intersection/of ?e]]
    ])


(def rule-child-intersections
  (vec (concat '[[(child ?e ?e2)
                  [?e :intersection/of _]
                  [?i :set/members ?e2]
                  ]]
                 child-rule
                 intersection-rule
                 ))
  )


(def child2
  '[[(child ?e ?e2)
     [?e :set/down ?e2]]
    [(child ?e ?e2)
     [?e2 :set/up ?e]]
    [(child ?e ?e2)
     [?e :set/down ?i]
     (child ?i ?e2)]
    [(child ?e ?e2)
     [?i :set/up ?e]
     (child ?i ?e2)]])


;;these kinds of circular dependencies are not allowed
;; (def child3 '[[(child ?e ?e2)
;;               [?e :set/down ?e2]]
;;              [(child ?e ?e2)
;;               [?e2 :set/up ?e]]
;;              [(child ?e ?e2)
;;               (child ?e ?i)
;;               (child ?i ?e2)]])


;; strange bug where getting too many results when not putting inside vector
;; or asking for the queried value
;; yeah, getting 10 and 11 as results too,
;; don't know why
;; I do know 10 and 11 are the only other children of any value

(deftest queries
  (let [q '[:find ?c
            :in $ % ?p
            :where (child ?p ?c)]

        q1 '[:find ?i
             :in $ % ?p
             :where (intersection ?p ?i)]]
    (testing "qs"
      (is (= (d/q q test-db child-rule 9) #{[10]}))
      (is (= (d/q q1 test-db intersection-rule 4) #{[7]}))
      (is (= " " rule-child-intersections))
      (is (= (d/q q test-db child2 9) #{[10][7][11][8]}))
      )))


(def query-atom (r/atom []))




(defcard-rg hey
  [:div (pr-str test-db)])

(def states
  ["Alabama" "Alaska" "Arizona" "Arkansas" "California"
   "Colorado" "Connecticut" "Delaware" "Florida" "Georgia" "Hawaii"
   "Idaho" "Illinois" "Indiana" "Iowa" "Kansas" "Kentucky" "Louisiana"
   "Maine" "Maryland" "Massachusetts" "Michigan" "Minnesota"
   "Mississippi" "Missouri" "Montana" "Nebraska" "Nevada" "New Hampshire"
   "New Jersey" "New Mexico" "New York" "North Carolina" "North Dakota"
   "Ohio" "Oklahoma" "Oregon" "Pennsylvania" "Rhode Island"
   "South Carolina" "South Dakota" "Tennessee" "Texas" "Utah" "Vermont"
   "Virginia" "Washington" "West Virginia" "Wisconsin" "Wyoming"])

(defn matcher [strs]
  (fn [text callback]
    (->> strs
         (filter #(str/includes? % text))
         (clj->js)
         (callback))))

(defn typeahead-mounted [this]
  (.typeahead (js/$ (r/dom-node this))
              (clj->js {:hint true
                        :highlight true
                        :minLength 1})
              (clj->js {:name "states"
                        :source (matcher states)})))




(def typeahead-value (r/atom nil))

(defn render-typeahead []
  [:input.typeahead
   {:type :text
    :on-select #(reset! typeahead-value (-> % .-target .-value))
    :placeholder "States"}])



(defn typeahead []
  (r/create-class
   {:component-did-mount typeahead-mounted
    :reagent-render render-typeahead}))


(defn home []
  [:div.flex.column
   (when-let [language @typeahead-value]
     [:label "selected: " language])
   [typeahead]])


(defcard-rg typeahead-home
  [home])





(def parent-child-db (d/db-with (d/empty-db {:title {:db/unique :db.unique/identity}
                                     :down {:db/valueType :db.type/ref
                                                   :db/cardinality :db.cardinality/many}
                                     :up  {:db/valueType :db.type/ref
                                                         :db/cardinality :db.cardinality/many}
                                     :set/intersections  {:db/valueType :db.type/ref
                                                  :db/cardinality :db.cardinality/many}
                                     :intersection/of  {:db/valueType :db.type/ref
                                                   :db/cardinality :db.cardinality/many}})
                        [{:title "October"
                          :down [{:title "Oct 1"
                                  :down [{:title "A"}
                                         {:title "B"}]}
                                 {:title "Oct 2"
                                  :down [{:title "C"}
                                         {:title "D"}]}
                                 {:title "Oct 3"
                                  :down [{:title "E"}
                                         {:title "F"}]}
                                 {:title "Oct 4"
                                  :down [{:title "G"}
                                         {:title "H"}]}
                                 {:title "Oct 5"
                                  :down [{:title "I"}
                                         {:title "J"}]}]}
                         {:title "Research"
                          :down [[:title "A"]
                                 [:title "C"]
                                 [:title "E"]]}
                         {:title "Development"
                          :down [[:title "B"]
                                 [:title "D"]
                                 [:title "F"]]}
                         {:title "Datascript"
                          :down [[:title "G"]
                                 [:title "A"]
                                 [:title "C"]]}
                         ]))

(defcard-rg testdb2
  [:div (pr-str parent-child-db)])



(def simple-child
  '[[(child ?p ?c)
     [?p :down ?c]]])

(deftest queries2
  (let [
        q '[:find ?p-names
            :in $ %
            :where
            [?parents :title ?p-names]
            (child ?parents ?c)]

        q1 '[:find ?c-title
             :in $ % $titles
             :where
             [$titles ?parent-titles]
             [?p :title ?parent-titles]
             (child ?p ?c)
             [?c :title ?c-title]
             ]
        q2 '[:find ?c-title
             :in $ % [?p1 ?p2]
             :where
             [?p :title ?p1]
             (child ?p ?c)
             [?c :title ?c-title]
             [?p22 :title ?p2]
             (child ?p22 ?c)
             ]]
    (testing "qs"
      (is (= (d/q q parent-child-db simple-child) #{[10]}))
      (is (= (d/q q1 parent-child-db simple-child [["Oct 1"]]) #{["A"]["B"]}))
      (is (= (d/q q2 parent-child-db simple-child ["Oct 1" "Research"]) #{["A"]}))
      )))






(deftest queries3
  (let [
        q '[:find ?p-names
            :in $ %
            :where
            [?parents :title ?p-names]
            (child ?parents ?c)]

        q1 '[:find ?c-title
             :in $ % $titles
             :where
             [$titles ?parent-titles]
             [?p :title ?parent-titles]
             (child ?p ?c)
             [?c :title ?c-title]
             ]
        q2 '[:find ?c-title
             :in $ % [?p1 ?p2]
             :where
             [?p :title ?p1]
             (child ?p ?c)
             [?c :title ?c-title]
             [?p22 :title ?p2]
             (child ?p22 ?c)
             ]]
    (testing "q"
      (is (= (d/q
              {:find '[?parents]
               :in '[$ %]
               :where '[[?pids :title ?parents]
                        (child ?pids ?cids)]}
              parent-child-db simple-child)
             #{["Oct 1"] ["Research"] ["Development"] ["Datascript"] ["October"]
               ["Oct 3"] ["Oct 4"] ["Oct 2"] ["Oct 5"]}))
      
      (is (= (d/q 
              {:find '[(pull ?cid [*] )]
                   :in '[$ % [?p]]
                   :where '[[?p1 :title ?p]
                            (child ?p1 ?cid)
                            [?cid :title ?c]]}
                  parent-child-db simple-child ["Oct 1"])
             #{["A"]["B"]}))
      (is (=  (multi-filter gotest ["A" "B"]) #{"A"}))
      (is (=  (mtest gotest ["A" "B"]) #{"A"}))
      (is (= (d/q q2 parent-child-db simple-child ["Oct 1" "Research"]) #{["A"]}))
      )))


;; (def gist-db (d/db-with (d/empty-db {:title {:db/unique :db.unique/identity}
;;                                      :above {:db/valueType :db.type/ref
;;                                              :db/cardinality :db.cardinality/many}
;;                                      :below {:db/valueType :db.type/ref
;;                                              :db/cardinality :db.cardinality/many}
;;                                      })
;;                         [{:title :A
;;                           :above {:title :B
;;                                   :above [{:title :C}]}}
;;                          {:title :D
;;                           :below [:B]}
;;                          ]))
;; (def below-rule
;;   '[[(below ?e ?e2)
;;      [?e :below ?e2]]
;;     [(child ?e ?e2)
;;      [?e2 :set/up ?e]]]
;;   )





;; (defn retrieve-entity [conn domain-key constraint-map]

;;   (let [constraints-w-ns constraint-map


;;         ;; We expect a structure like... ((:posts/title t) (:posts/content-type c/t))... at the end, we need to double-quote the name
;;         names-fn #(-> % first name (string/split #"/") first (->> (str "?")) symbol #_(->> (list (symbol 'quote))))
;;         param-names (map names-fn
;;                            (seq constraints-w-ns))
;;         param-values (into [] (map last (seq constraints-w-ns)))


;;         ;; Should provide constraints that look like: [[?e :posts/title ?title] [?e :posts/content-type ?content-type]]
;;         constraints-final (->> constraints-w-ns
;;                                seq
;;                                (map (fn [inp]
;;                                       ['?e (first inp) (names-fn inp)] ))
;;                                (into []))

;;         ;;
;;         expression-final {:find ['?e]
;;                           :in ['$ (into [] param-names)]
;;                           :where constraints-final}

;;         ;;
;;         the-db (datomic.api/db conn)]

;; (d/q expression-final the-db param-values) ))
