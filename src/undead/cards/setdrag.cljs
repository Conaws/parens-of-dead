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
                        "none")
                      :background-color "green"
                      :opacity
                      (if (:dragging @s)
                        "0.9"
                        "1")
                      }
              :on-drag-enter (fn [e]
                               (swap! s update :list (fn [l]
                                                         (splice :placeholder
                                                                 l
                                                                 i)))
                               )
              :on-drag-start (fn [e]
                               (swap! s assoc :dragging v)
                               )
              :on-drag-end (fn [e]
                             (do
                               (swap! s update :list (fn [l]
                                                       (swap-in-vector l :placeholder (:dragging @s))))
                               (swap! s assoc :over false
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


