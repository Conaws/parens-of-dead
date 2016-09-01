(ns undead.cards
  (:require
   [devcards.core :as dc] ; <-- here
   [undead.cards.zip]
   [undead.cards.drag]
   [undead.cards.zombies]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg test
  [:div "hello"])





