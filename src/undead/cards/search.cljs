(ns undead.cards.search
  (:require [cljs-time.core :refer [now]]
            [clojure.set :as set]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [v-box box h-box]]
            [reagent.core :as r]
            [clojure.string :as str])
  (:require-macros
   [cljs.test  :refer [testing is are]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack]]))


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




;; strange bug where getting too many results when not putting inside vector
;; or asking for the queried value
;; yeah, getting 10 and 11 as results too,
;; don't know why
;; I do know 10 and 11 are the only other children of any value

(deftest queries
  (let [q '[:find ?c
            :in $ % ?p
            :where (child ?p ?c)]]
    (testing "qs"
      (is (= (d/q q test-db child-rule 9) #{[10]})))))



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
