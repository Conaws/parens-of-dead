(ns undead.cards.grid
  (:require [cljs-time.core :refer [now]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [button v-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [e conn]]
            [clojure.set :as set])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(def datatom (r/atom (now)))

(defonce newconn conn)
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
                  [:node/title "Elon Musk"]
                  ]}
   {:db/id -6
    :node/type :set
    :node/title "Investor Sets"
    :set/members [-5 -4]
    }
   ])


(d/transact! newconn grid-transaction)

(defn simple-table []
  (let [i (posh/pull newconn '[:node/title {:set/members ...}] [:node/title "Investors"])
        sets (posh/pull newconn '[:node/title :node/type {:set/members ...}] [:node/title "Investor Sets"])]
    [:div
     [:table.bblack
      [:thead [:tr [:th "Investors"][:th]
               (for [m (:set/members @sets)]
                 [:th (:node/title m)])]]
      [:tbody
       (for [m (:set/members @i)]
         [:tr [:th (:node/title m)][:th]
          (for [m (:set/members @sets)]
            [:th (:node/title m)])
          ]
         )

       ]
      
      ]
     (pr-str @i)
     ]))


(defn testa []
  (fn []
    [v-box
     :gap "10px"
     :children
     [[rc/datepicker-dropdown :model datatom
       :on-change #(reset! datatom %)]
      [rc/datepicker :model datatom
       :on-change #(reset! datatom %)]
      [rc/title :label "title"
       :level :level1
       :underline? true]
      [button :label (pr-str @newconn)]
      [simple-table]
      [rc/scroller
       :v-scroll :auto
       :height "300px"
       :width "55px"
       :child [:div (repeat 1000 "lorem ipsum dolar")]]]
     ]))
(defcard-rg tabledrawa
 [testa] 
  )











