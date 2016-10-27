(ns undead.cards.poem
  (:require
   [goog.i18n.DateTimeFormat :as dtf]
   [posh.core :as posh :refer [posh!]]
   [cljs-time.core :as time :refer [now]]
   [keybind.core :as keys]
   [cljs.pprint :refer [pprint]]
   [datascript.core :as d]
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   [clojure.set :as set]
   [re-com.core :as rc])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]) )





(def poem-schema {:set/subsets        {:db/valueType   :db.type/ref
                                        :db/cardinality :db.cardinality/many}
                  :set/attributes        {:db/valueType   :db.type/ref
                                      :db/cardinality :db.cardinality/many}
                  :set/members          {:db/valueType   :db.type/ref
                                    :db/cardinality :db.cardinality/many}})





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
    :set/title "Poem"
    :set/members [-1]}

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
                   :set/members [-9]}]}
   {:db/id -9
    :set/title "Hafiz"
    :set/members [-1 -14]}

   {:db/id -10
    :set/title "Theme"
    :set/members [-11]
    :set/subsets [-11]}
   {:db/id -11
    :set/title "The Divine"}

   {:db/id -12
    :set/title "Type"
    :set/subsets [-13]}
   {:db/id -13
    :set/title "Poem"
    :set/attributes [-10 -8 -3]}

   {:db/id -14
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
             :set/subsets [{:set/title "The Divine"}]}]
           (d/q
            '[:find [(pull ?attributes [:set/title :set/members {:set/subsets ...}] ) ...]
              :in $ ?type
              :where [?e :set/title ?type]
              [?e :set/attributes ?attributes]
              ]
            poem-db
            "Poem")))))




(def attr-query '[:find [(pull ?attributes
                               [:set/title :set/members
                                {:set/subsets ...}]) ...]
                  :in $ ?type
                  :where [?e :set/title ?type]
                  [?e :set/attributes ?attributes]
                  ])






(defn demo-filter [{:keys [set/title set/subsets set/members] :as n} ]
  (let [my-atom (r/atom false)]
    (fn []
      (let [member-count (count members)]
        [:div
         [rc/h-box
          :gap "15px"
          :children [[rc/checkbox
                      :label title
                      :model my-atom
                      :on-change #(reset! my-atom %)]
                     (when (< 0 member-count)
                       [:span member-count])]]
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
