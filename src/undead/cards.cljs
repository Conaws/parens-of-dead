(ns undead.cards
  (:require
   [devcards.core :as dc] ; <-- here
   [undead.cards.zip]
   [undead.cards.drag]
   [undead.cards.setdrag]
   [undead.cards.search]
   [undead.cards.logic]
   [undead.cards.labels]
   [undead.cards.grid]
   [undead.cards.cloze]
   [undead.cards.attrs]
   [undead.cards.fire2]
   [undead.cards.yin]
   [undead.cards.demo]
   [undead.cards.dstests]
   [undead.cards.auto]
   [undead.cards.multi]
   [undead.cards.d3]
   [undead.cards.keys]
   [undead.cards.animate]
   [undead.cards.thenvenn.day]
   [undead.cards.zombies]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg test
  [:div "hello"])





