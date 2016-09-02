(ns undead.cards.setdrag
  (:require [reagent.core :as r])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core :refer [deftest defcard-rg]]))

(def app-state (r/atom {:list (vec (range 11))
                       :over -1
                       :dragging -1}))




(defn placeholder [ s]
  [:li {:style {:background "rgb(255,240,120)"}}
   "Place here"]
  )



(defn listitem [i s]
    (fn [i s]
      (if (= i (:over @s))
        [placeholder s]
        [:li {:data-id i
              :draggable true
              :style {:display
                      (if (= i (:dragging @s))
                        "none")
                      :background-color
                      (if (:dragging @s)
                        "green"
                        "blue")
                      }
              :on-drag-enter (fn [e]
                               (swap! s assoc :over i)
                               )
              :on-drag-start (fn [e]
                               (swap! s assoc :dragging i)
                               )
              :on-drag-end (fn [e]
                             (do
                                 (swap! s update :list (fn [l]
                                                       (assoc l
                                                              (:over @s) i
                                                              i (:over @s))
                                                       ))
                                 (swap! s assoc :over -1))
                             (js/console.log e)
                             )}
         i]))) 


(deftest vectest
  (testing "assoc"
    (is (= [1 2 99] (assoc [1 2] 2 99) ))))



(defn list-render [state]
    (fn [state]
      [:ol
       (for [i (:list @state)]
        ^{:key i}[listitem i state])]))

(defcard-rg listcard
  [list-render app-state]
  app-state
  {:inspect-data true
   :history true})

