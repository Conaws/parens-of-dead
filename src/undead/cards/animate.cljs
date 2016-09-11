(ns undead.cards.animate
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(defn css-test [state]
  (fn [state]
    [:div.flex
     (for [i (:nodes @state)]
       [:div.cell2
        {:style {:background-color (:color @state "blue")}}
        [:button {:on-click #(swap! state assoc :color "red")} "red"]
        [:button {:on-click #(swap! state assoc :color "green")} "green"]
        [:button {:on-click #(swap! state assoc :color "white")} "white"]
        [:button {:on-click #(swap! state assoc :color "black")} "black"]
        [:button {:on-click #(swap! state assoc :color "blue")} "blue"]
        ])
     [:div.cell
      "a b c"]
     ])
  )

(defonce animatestate (r/atom {:nodes (range 5)
                               :color "green"}))

(defcard-rg cellll
  [css-test animatestate]
  animatestate
  {:inspect-data true
   :history true} )


(defn rdelay []
  [:div.cell {:style {:width "30px"
                      :transition-property "margin-left"
                      :transition-duration "3s"
                      :margin-left "400px"
                      }} "hey"]

  )

(defcard-rg test
  [rdelay])


(def slidatom (r/atom {:hover false}))

(defn slidein []
  (let [slidatom (r/atom {:hover false})]
    (fn []
      [:div.oneS.tall.bblack
       {:class (if (:hover @slidatom) "column1" "column2" )
        :on-mouse-enter #(swap! slidatom assoc :hover true)
        :on-mouse-leave #(swap! slidatom assoc :hover false)
        }
       (pr-str @slidatom)
       ]))
  )

(defcard-rg slidein-demo
  [slidein])

