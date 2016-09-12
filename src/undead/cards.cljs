(ns undead.cards
  (:require
   [devcards.core :as dc] ; <-- here
   [undead.cards.zip]
   [undead.cards.drag]
   [undead.cards.setdrag]
   [undead.cards.logic]
   [undead.cards.auto]
   [undead.cards.keys]
   [undead.cards.animate]
   [undead.cards.zombies]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg test
  [:div "hello"])





