(ns undead.cards.setdrag
  (:require [reagent.core :as r])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core :refer [deftest defcard-rg]]))

(defonce app-state (r/atom {:list (vec (range 11))
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
   [:ul
    [:li i]
    [:li v]]]
  )



(defn listitem [i v s]
    (fn [i v s]
      (if (= i (:over @s))
        [placeholder (:dragging @s) v]
        [:li {:data-id i
              :draggable true
   ;           :class-name "placeholder"
              :style {:display
                      (if (= v (:dragging @s))
                        "none")
                      :background-color "green"
                      :opacity
                      (if (:dragging @s)
                        "0.7"
                        "1")
                      }
              :on-drag-enter (fn [e]
                               (swap! s assoc :over i)
                               )
              :on-drag-start (fn [e]
                               (swap! s assoc :dragging v)
                               )
              :on-drag-end (fn [e]
                             (do
                               (swap! s update :list (fn [l]
                                                       (if (:over @s)
                                                         (splice (:dragging @s)
                                                                 l
                                                                 (:over @s)))))
                               (swap! s assoc :over false
                                      :dragging false)))}
         v]))) 




(deftest vectest
  (testing "assoc"
    (is (= [1 3 2] (splice 3 [1 2] 1)))
    (is (= [3 1 2] (splice 3 [1 2] 0)))
    (is (= [1 2 99] (assoc [1 2] 2 99) ))))



(defn list-render [state]
    (fn [state]
      [:ol
       (for [[i v] (map-indexed vector (:list @state))]
        ^{:key i}[listitem i v state])]))

(defcard-rg listcard
  [list-render app-state]
  app-state
  {:inspect-data true
   :history true})



(defn ainc [arr x] 
  (let [v (clj->js arr)
        xpos (.indexOf v x)]
    (if-let [swapval (aget v (dec xpos))]
      (do (aset v xpos swapval)
          (aset v (dec xpos) x)
          (js->clj v))
      arr)))

(defn adec [arr x] 
  (let [v (clj->js arr)
        xpos (.indexOf v x)]
    (if-let [swapval (aget v (inc xpos))]
      (do (aset v xpos swapval)
          (aset v (inc xpos) x)
          (js->clj v))
      arr)))


