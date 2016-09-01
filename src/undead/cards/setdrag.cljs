(ns undead.cards.setdrag
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))




(defn placeholder [ s]
  [:li {:style {:background "rgb(255,240,120)"}

             :on-drag-leave (fn [e]
                           (swap! s assoc :over false)
                           ) 

              
        }
   "Place here"]
  )



(defn listitem [i]
  (let [s (r/atom {:over false
                   :dragging false})]
    (fn [i]
      (if (:over @s)
        [placeholder s]
        [:li {:data-id i
              :draggable true
              :style {:display
                      (if (or (:over @s)(:dragging @s))
                        "none")
                      :background-color
                      (if (:dragging @s)
                        "green"
                        "blue")
                      }
              :on-drag-enter (fn [e]
                               (swap! s assoc :over true)
                               )
              :on-drag-start (fn [e]
                               (swap! s assoc :dragging true)
                               )
              :on-drag-end (fn [e]
                             (swap! s assoc :dragging false)
                             (js/console.log e)
                             )}
         i]))) )





(defn list-render [l]
  (let [state (r/atom (vec l))]
    (fn [l]
      [:ol

       (for [i l]
        ^{:key i}[listitem i])])))

(defcard-rg listcard
  [list-render (range 10)])

