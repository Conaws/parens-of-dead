(ns undead.cards.setdrag
  (:require [reagent.core :as r])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core :refer [deftest defcard-rg]]))

(defonce app-state (r/atom {:list (vec (range 11))
                       :over -1
                       :dragging -1}))

(defn splice [x vctr pstn]
  (let [vctr (vec (filter #(not (= x %)) vctr))
        start (subvec vctr 0 pstn)
        end (subvec vctr pstn)]
    (vec (concat (conj start x)  end))))


(defn placeholder [s]
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
                                                       (if (:over @s)
                                                         (splice (:dragging @s)
                                                                 l
                                                                 (:over @s)))))
                               (swap! s assoc :over false
                                      :dragging false)))}
         i]))) 




(deftest vectest
  (testing "assoc"
    (is (= [1 3 2] (splice 3 [1 2] 1)))
    (is (= [3 1 2] (splice 3 [1 2] 0)))
    (is (= [1 2 99] (assoc [1 2] 2 99) ))))



(defn list-render [state]
    (fn [state]
      [:ol
       (for [[x i] (map-indexed vector (:list @state))]
        ^{:key x}[listitem i state])]))

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


