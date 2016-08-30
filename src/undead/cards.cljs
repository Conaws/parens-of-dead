(ns undead.cards
  (:require
   [devcards.core :as dc] ; <-- here
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg test
  [:div "hello"])
