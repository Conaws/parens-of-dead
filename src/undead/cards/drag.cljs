(ns undead.cards.drag
  (:require [clojure.pprint :refer [pprint]]
            [clojure.zip :as z]
            [datascript.core :as d]
            [posh.reagent :as posh :refer [posh!]]
            [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))



(defn home-render []
  [:div.ui-widget-content {:style {:width "150px" 
                                   :height "150px" 
                                   :padding "0.5em"}}
   [:p "Drag me around"]])


#_(defn home-did-mount []
  (js/$ (fn []
          (.draggable (js/$ "#draggable")))))


(defn home-did-mount [this]
  (.draggable (js/$ (r/dom-node this))))


(defn home []
  (r/create-class {:reagent-render home-render
                         :component-did-mount home-did-mount}))



(defcard-rg homedrag
  [home])

