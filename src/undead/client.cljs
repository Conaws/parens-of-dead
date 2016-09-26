(ns undead.client
  (:require [reagent.core :as r]))




(r/render-component
 [:div "now we're on it"]
 (.getElementById js/document "main"))

