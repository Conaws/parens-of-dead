(ns undead.cards.attrs
  (:require
   [cljs.tools.reader.edn :as edn]
   [posh.core :as posh :refer [posh!]]
   [datascript.core :as d]
   [clojure.set :as set]
   [reagent.core :as r])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack]]))

(def simp-schema {:set/title        {:db/unique :db.unique/identity}
                  :element/prototype-of        {:db/valueType   :db.type/ref
                                                :db/cardinality :db.cardinality/many}
                  :element/similar-to        {:db/valueType   :db.type/ref
                                              :db/cardinality :db.cardinality/many}
                  :set/attributes   {:db/cardinality :db.cardinality/many}
                  :set/members          {:db/valueType   :db.type/ref
                                      :db/cardinality :db.cardinality/many}})



(def test-db (d/db-with (d/empty-db simp-schema)
                            [{:set/title "Poem"
                              :set/attributes [:u/Author
                                               :u/Text
                                               :u/Title
                                               :u/Date.Created]}
                             {:db/id -1
                              :u/Author "Conor White-Sullivan"
                              :u/Text "I love the wildness of clojure"
                              :u/Date.Created "The continuous Present"
                              :u/Title "Where the Wild things Really Are"
                              }]))


(defonce conn (d/conn-from-db test-db))
(posh! conn)


(deftest valid-schema
  (testing "custom attrs"
    (is (= [] (d/datoms @conn :eavt)))
    (is (= [] (posh/q conn '[:find ?a ?v
                             :in $ ?id
                             :where [?id ?a ?v]]
                      2)))
    (is (= [] (posh/q conn '[:find ?a ?v
                             :in $ ?id
                             :where [?id ?a ?v]]
                      2)))))



(deftest poshtest
  (let [a (d/conn-from-db test-db)
        b conn]
    (testing "x"
      (is (= a b)))
    ))
