(ns undead.db
  (:require
   [cljs.tools.reader.edn :as edn]
   [posh.core :refer [pull q posh! transact!]]
   [datascript.core :as d]
   [cljs.spec :as s]
   [clojure.set :as set]
   [reagent.core :as r])
  (:require-macros
   [reagent.ratom :refer [reaction]]))



;;; specs



(def ds-db? #(instance? datascript.db/DB %))
(def ratom? (partial instance? reagent.ratom/RAtom))
(def atom? (partial instance? cljs.core/Atom))
(s/def ::ds  ds-db?)



(def schema {:node/title        {:db/unique :db.unique/identity}
             :node/prototype-of        {:db/valueType   :db.type/ref
                                        :db/cardinality :db.cardinality/many}
             :node/similar-to        {:db/valueType   :db.type/ref
                                      :db/cardinality :db.cardinality/many}
             :set/attributes   {:db/cardinality :db.cardinality/many}
             :set/members          {:db/valueType   :db.type/ref
                                    :db/cardinality :db.cardinality/many}})


#_(def schema {:set/title        {:db/unique :db.unique/identity}
             :set/down          {:db/valueType   :db.type/ref
                                 :db/cardinality :db.cardinality/many}})




(def test-db (d/db-with (d/empty-db schema)
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

(def sample-conn (d/create-conn schema))
(posh! sample-conn)







(def default-db
  {:name "re-frame"
   :user "Public"
   :atomic-test (r/atom {:a (r/atom "B")})
   :atomic-conn (r/atom sample-conn)
   :conn "Loading"

   })




;; (defn init [conns uid]
;;   (let [conn (d/create-conn schema)]
;;     (posh! conn)
;;     (swap! conns assoc uid conn)
;;     conn))

;; (defn add-conn [conns uid db]
;;   (swap! conns assoc uid
;;          (let [conn (d/create-conn schema)]
;;            (when db
;;              (d/reset-conn! conn (edn/read-string {:readers d/data-readers} db)))
;;            (posh! conn)
;;            conn)))


#_(defn once
  "Retreives the firebase state at path as a ratom that will be set when the state arrives."
  [path]
  (let [a (r/atom nil)]
    (.once
     (db-ref path)
     "value"
     (fn received-db [snapshot]
       (reset! a (.val snapshot))))
    a))
