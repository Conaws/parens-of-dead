(ns undead.cards.setdrag
  (:require [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]] 
            [reagent.core :as r])
  (:require-macros
   [cljs.test :refer [is testing]]
   [devcards.core :refer [defcard-rg deftest]]))

(defonce app-state (r/atom {:list (vec (take 11 "abcdefghijkl"))
                       :over -1
                       :dragging false}))

(defn splice [x vctr pstn]
  (let [vctr (vec (filter #(not (= x %)) vctr))
        start (subvec vctr 0 pstn)
        end (subvec vctr pstn)]
    (vec (concat (conj start x)  end))))




(defn placeholder [i v]
  [:li {:style {; :height "2em"
                :background "rgb(255,240,120)"}}
   v]
  )

(defn without [x v]
  (filter #(not (= x %)) v)
  )

(defn replace-v [v outgoing incoming]
  (let [xpos (.indexOf v outgoing)]
    (assoc v xpos incoming))
  )

(defn swap-in-vector [v a b]
  (-> (without b v)
      vec
      (replace-v a b)
    ))


(defn listitem [i v s]
    (fn [i v s]
      (if (= :placeholder v)
        [placeholder (:dragging @s) v]
        [:li {:data-id i
              :draggable true
   ;           :class-name "placeholder"
              :style {:display
                      (if (= v (:dragging @s))
                        "none"
                        )
                      :background-color "green"
                      :border "2px solid white"
                      :margin "5px"
                      :opacity
                      (if (:dragging @s)
                        "0.9"
                        "1")}
              :on-drag-enter (fn [e]
                               (swap! s update :list (fn [l]
                                                         (splice :placeholder
                                                                 l
                                                                 i))))
              :on-drag-start (fn [e]
                               (swap! s assoc :dragging v
                                      :oldlist (:list @s)))
              :on-drag-end (fn [e]
                             #_(do
                               (swap! s update :list (fn [l]
                                                       (swap-in-vector
                                                        l
                                                        :placeholder
                                                        (:dragging @s))))
                               (swap! s assoc
                                      :over false
                                      :dragging false)))}
         v]))) 




(deftest vectest
  (testing "assoc"
    (is (= 0 (.indexOf [1 2] 1)))
    (is (= 1 (get [1 2] 0)))
    (is (= [0 2] (replace-v [1 2] 1 0)))
    (is (= [0 2 1] (swap-in-vector [:boom 2 1 0] :boom 0)))
    (is (= [3 1 2] (splice 3 [1 2] 0)))
    (is (= [3 1 2] (splice 3 [1 2] 0)))
    (is (= [1 2 99] (assoc [1 2] 2 99) ))))



(defn list-render [s]
    (fn [s]
      [:ol {:style {:border "2px solid blue"}
            :on-drag-end  (fn [e]
                           (do
                             (swap! s update :list (fn [l]
                                                     (swap-in-vector
                                                      l
                                                      :placeholder
                                                      (:dragging @s))))
                             (swap! s assoc
                                    :over false
                                    :dragging false)))}
       (for [[i v] (map-indexed vector (:list @s))]
        ^{:key i}[listitem i v s])]))

(defcard-rg listcard
  [list-render app-state]
  app-state
  {:inspect-data true
   :history true})



(def schema {:set/members {:db/valueType :db.type/ref
                             :db/cardinality :db.cardinality/many}
             :logic/not  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}})


(def lconn (d/create-conn schema))
(posh! lconn)


(def sample-nodes2
  [{:db/id      1
    :node/title "A"}
   {:db/id      2
    :node/title "B"}
   {:db/id      3
    :node/title "C"}
   {:db/id      4
    :logic/type :not
    :node/title "Not A"
    :logic/not 1}
   {:db/id       5
    :logic/type  :and
    :logic/title  "A and B"
    :set/members #{1 3}}
   {:db/id      6
    :logic/type :if-then
    :logic/title "If (A and B) then C"
    :logic/if   #{5}
    :logic/then #{3}}
   {:db/id 7
    :logic/type :or
    :logic/title "(B or C)"
    :set/members #{2 3}}
   {:db/id 8
    :node/title "D"}
   {:db/id 9
    :logic/type :or
    :logic/title "(B or C) or A"
    :set/members #{1 7}}
   {:db/id 10
    :logic/type :if-then
    :logic/title "If ((B or C) or A) then D"}
   ])


(d/transact! lconn sample-nodes2)



(defn and-render [conn id]
  (let [members (pull conn '[:node/title {:set/members ...}] id)]
    (fn []
      [:div.bblack {:style
             {:background-color "#fefefe"}}
       [:h1 "∧"]
       (pr-str @members)])
    ))





(defn nodes-render [conn]
      (let [n (q conn '[:find ?eid ?val ?text 
                   :where [?eid ?val ?text ]]
                 )
            all-ents (q conn '[:find (pull ?e [*])
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
            (for [[i] @all-ents]
              (condp = (:logic/type i)
                :and [and-render conn (:db/id i)]
                :or [:div
                     [:h1 "or"]
                     (pr-str i)]
                [:div "not and"]))]
           [:button {:style {:grid-area "other"}}]
           [:div {:style {:border "2px solid blue"
                          :display "flex"
                          :background-color "blue"
                          :padding "5px"
                          :color "white"
                          :overflow "scroll"
                          :flex-flow "column wrap"
                          :width "150px"
                          :grid-area "items"}}
            (for [[i] @all-ents]
              [:div
               {:draggable true}
               (pr-str (:node/title i))])]]
          )))


(defcard-rg nodestest
  [nodes-render lconn])
