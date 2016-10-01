(ns undead.cards.dstests
  (:require
   [datascript.core :as d]
   [clojure.test :as t])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core :refer [defcard-rg deftest]]))


;; (deftest test-resolve-eid-refs
;;   (let [conn (d/create-conn {:friend {:db/valueType :db.type/ref
;;                                       :db/cardinality :db.cardinality/many}})
;;         tx   (d/transact! conn [{:name "Sergey"
;;                                  :friend [-1 -2]}
;;                                 [:db/add -1 :name "Ivan"]
;;                                 [:db/add -2 :name "Petr"]
;;                                 [:db/add -4 :name "Boris"]
;;                                 [:db/add -4 :friend -3]
;;                                 [:db/add -3 :name "Oleg"]
;;                                 [:db/add -3 :friend -4]])
;;         q '[:find ?fn
;;             :in $ ?n
;;             :where [?e :name ?n]
;;                    [?e :friend ?fe]
;;                    [?fe :name ?fn]]]
;;     (is (= (:tempids tx) { -1 2, -2 3, -4 4, -3 5, :db/current-tx (+ d/tx0 1) }))
;;     (is (= (d/q q @conn "Sergey") #{["Ivan"] ["Petr"]}))
;;     (is (= (d/q q @conn "Boris") #{["Oleg"]}))
;;     (is (= (d/q q @conn "Oleg") #{["Boris"]}))))


(deftest test-resolve-eid-refs-deep-map
  (let [conn (d/create-conn {:name {:db/unique :db.unique/identity}
                             :friend {:db/valueType :db.type/ref
                                      :db/cardinality :db.cardinality/many}})
        tx   (d/transact! conn [{:name "Sergey"
                                 :friend [{:name "Ivan"}
                                          {:name "Petr"}]}
                                {:name "Oleg"
                                 :friend [{:name "Dmitri"
                                           :friend [{:name "Sven"
                                                     :friend [{:name "John"
                                                               :friend [
                                                                        {:name "Sergey"}
                                                                        ]
                                                               :_friend [
                                                                         {:name "Dmitri"}]}
                                                              {:name "Petr"}]
                                                     }]}]
                                 :_friend [{:name "Boris"}]}]
                          )
        q '[:find ?fn
            :in $ ?n
            :where [?e :name ?n]
            [?e :friend ?fe]
            [?fe :name ?fn]]]
    #_(is (= (:tempids tx) { -1 2, -2 3, -4 4, -3 5, :db/current-tx (+ d/tx0 1) }))
    (is (= (d/q q @conn "Sergey") #{["Ivan"] ["Petr"]}))
    ;; true
    (is (= (d/q q @conn "Boris") #{["Oleg"]}))
    ;; true
    ;; this shows that :_ does build relationship on reverse lookups
    ;; and can create the new entity at the same time
    (is (= (d/q q @conn "Oleg") #{["Dmitri"]}))
    ;; true
    (is (= (d/q q @conn "Sven") #{["John"][ "Petr"]} ))
    ;; true
    (is (= (d/pull @conn '[:name {:_friend 1}] [:name "Petr"])
           {:name "Petr"
            :_friend [{:name "Sergey"}
                      {:name "Sven"}]}))
    ;; true - shows that you can create relationships on a deep add, to existing ents
    (is (= (d/q q @conn "Dmitri") #{["Sven"] ["John"]}))
    ;; fails, only Sven is a friend of Dmitri
    ;; shows that if you are going to be doing a deep map transaction, each path
    ;; needs to only go in one direction
    (is (= (d/q q @conn "John") #{["Sergey"]}))
    ;; true, shows that the deep add works when moving in one direction
    ))



;; (deftest test-resolve-eid-refs-deep-map-with-lookup
;;   (let [conn (d/create-conn {:name {:db/unique :db.unique/identity}
;;                              :friend {:db/valueType :db.type/ref
;;                                       :db/cardinality :db.cardinality/many}})
;;         tx   (d/transact! conn [{:name "Sergey"
;;                                  :friend [{:name "Ivan"}
;;                                           {:name "Petr"}]}
;;                                 {:name "Oleg" :_friend [{:name "Boris"}]}]
;;                           )
;;         q '[:find ?fn
;;             :in $ ?n
;;             :where [?e :name ?n]
;;             [?e :friend ?fe]
;;             [?fe :name ?fn]]]
;;     (is (= (d/q q @conn "Sergey") #{["Ivan"] ["Petr"]}))
;;     ;; succeeds
;;     (is (= (d/q q @conn "Boris") #{["Oleg"]}))
;;     ;; succeeds
;;     (is (= (d/q q @conn "Oleg") #{["Boris"]}))
;;     ;; fails, returns #{}
;;     ))

;; (deftest test-resolve-eid-refs-deep-map-with-lookup2 
;;   (let [conn (d/create-conn {:name {:db/unique :db.unique/identity}
;;                              :friend {:db/valueType :db.type/ref
;;                                       :db/cardinality :db.cardinality/many}})
;;         tx   (d/transact! conn [{:name "Sergey"
;;                                  :friend [{:name "Ivan"}
;;                                           {:name "Petr"}]}
;;                                 {:name "Boris"}
;;                                 {:name "Oleg" :_friend [{:name "Boris"}]}]
;;                           )
;;         q '[:find ?fn
;;             :in $ ?n
;;             :where [?e :name ?n]
;;             [?e :friend ?fe]
;;             [?fe :name ?fn]]]
;;     (is (= (d/q q @conn "Sergey") #{["Ivan"] ["Petr"]}))
;;     ;; succeeds
;;     (is (= (d/q q @conn "Boris") #{["Oleg"]}))
;;     ;; succeeds
;;     (is (= (d/q q @conn "Oleg") #{["Boris"]}))
;;     ;; fails, returns #{}
;;     ))
