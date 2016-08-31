(ns undead.cards.zombies
  (:require
   [devcards.core :as dc] ; <-- here
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg tes2t
  [:div "hello"])


(def game {:board [{:face :h0 :matched? true} {:face :h0 :matched? true} {:face :h1} {:face :h1} {:face :h2} {:face :h2} {:face :h3} {:face :h3} {:face :h4} {:face :h4}]

           :sand (concat (repeat 10 :gone)
                         (repeat 20 :remaining))
           :foggy? false})


(defn Cell [cell]
  [:div.cell
   [:div {:className (str "tile"
                          (when (:revealed? cell) " revealed")
                          (when (:matched? cell) " matched")
                          )}]
   [:div {:className "front"}]
   [:div.cell {:className "back"}
    (pr-str (:face cell))
    ]])


(defcard-rg celltest
  [Cell {:matched? true :face :h2}])



(defn Line [cells]
  [:div.line
   (for [[i c] (map-indexed vector cells)]
     ^{:key (str i c)}[Cell c])])


(defn Board [cells]
  [:div.flex
   (for [l (partition 4 cells)]
     ^{:key l}[Line l])]
  )


(defcard-rg boardtes
  [Board (:board game)])


(defn Timer [{:keys sand index}]
  [:div {:className (str "timer timer-" index)}
   (for [s sand]
     [:div {:className (str "sand " (name s))}])])



