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



(defn draggable-render []
  [:div.ui-widget-content {:style {:width "100px"
                                   :height "100px" 
                                   :padding "0.5em"
                                   :float "left" 
                                   :margin "10px 10px 10px 0"}}
   [:p "Drag me to my target"]])

(defn draggable-did-mount [this]
  (.draggable (js/$ (r/dom-node this))))

(defn draggable []
  (r/create-class {:reagent-render draggable-render
                   :component-did-mount draggable-did-mount}))



(def app-state (r/atom {:drop-area {:class "ui-widget-header"
                                    :text "Drop here"}}))



(defn drop-area-render []
  (let [class (get-in @app-state [:drop-area :class])
        text (get-in @app-state [:drop-area :text])]
    [:div {:class class
           :style {:width "150px" 
                   :height "150px"
                   :padding "0.5em" 
                   :float "left" 
                   :margin "10px"}}
     [:p text]]))



(defn drop-area-did-mount [this]
  (.droppable (js/$ (r/dom-node this))
              #js {:drop (fn []
                           (swap! app-state assoc-in [:drop-area :class] "ui-widget-header ui-state-highlight")
                           (swap! app-state assoc-in [:drop-area :text] "Dropped!"))}))



(defn drop-area []
  (r/create-class {:reagent-render drop-area-render
                   :component-did-mount drop-area-did-mount}))


(defcard-rg dropable
  [:div
   [draggable]
   [drop-area]])


