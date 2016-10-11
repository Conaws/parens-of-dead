(ns undead.cards.animate
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [reanimated.core :as anim]
            [reagent.core :as r])
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


(reg-event-db
 :assoc
 (fn [db [_ p v]]
   (assoc-in db p v)))

(reg-event-db
 :show-panel
 (fn [db _]
   (assoc-in db [:helm-panel :showing] true)))


(defn show-panel []
  (dispatch [:assoc [:helm-panel :showing] true]))

(defn hide-panel []
  (dispatch [:assoc [:helm-panel :showing] false]))

(reg-sub
 :helm-panel
 (fn [db _]
   (:helm-panel db)))


(defn hover [in out]
  {:on-mouse-enter in
   :on-mouse-leave out
   })

; (dispatch [:show-panel])

(defn helm-panel []
  (let [panel (subscribe [:helm-panel])]
    (fn []
      [:div.oneS.bblack.tall
       (merge (hover show-panel
                     hide-panel
                     )
              {:class (if (:showing @panel) "column2" "column1")})
       (pr-str @panel)]
      )))




(defcard-rg slide-up
  [helm-panel])


(def samples (r/atom {:a 1 :b 2 :c 3 :d 4}))


(defn anim1 [samples]
  [:div
   [:button
    {:on-click #(swap! samples assoc (rand-int 10000) (str (rand-int 10000) "Node"))}]
   [anim/css-transition-group
    {:transition-name "todo"
     :transition-enter-timeout 500
     :transition-leave-timeout 500
     :component "ul"
     :class "todo-list"}
    (doall
     (for [[idx item] @samples]
       [:li.todo
        {:key idx}
        item
        [:button.btn
         {:style {:float "right"}
          :on-click #(swap! samples dissoc idx)}
         "X"]]
       ))]])


(defcard-rg anim-c
  [anim1 samples]
  samples
  {:inspect-data true
   :history true})

(defn anim2 [samples]
  [:div
   [:button
    {:on-click #(swap! samples assoc (rand-int 10000) (str (rand-int 10000) "Node"))}]
   [anim/css-transition-group
    {:transition-name "slide"
     :transition-enter-timeout 1000
     :transition-leave-timeout 1000
     :component "ul"
     :class "todo-list"}
    (doall
     (for [[idx item] (reverse @samples)]
       [:li.slider
        {:key idx}
        item
        [:button.btn
         {:style {:float "right"}
          :on-click #(swap! samples dissoc idx)}
         "X"]]
       ))]])


(defcard-rg anim-slide-c
  [anim2 samples]
  samples
  {:inspect-data true
   :history true})
