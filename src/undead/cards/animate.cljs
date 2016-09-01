(ns undead.cards.animate
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(defn rdelay []
  [:div.cell {:style {:width "30px"
                      :transition-property "margin-left"
                      :transition-duration "3s"
                      :margin-left "400px"
                      }} "hey"]

  )

(defcard-rg test
  [rdelay])

