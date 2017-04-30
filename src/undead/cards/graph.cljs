(ns undead.cards.graph
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(def graph-data
  "Contains the nodes and edges for the graph."
  [{:data {:id "0"}}
   {:data {:id "1"}}
   {:data {:id "2"}}
   {:data {:id "3"}}
   {:data {:id "4"}}
   {:data {:id "5" :source "0" :target "2"}}
   {:data {:id "6" :source "0" :target "3"}}
   {:data {:id "7" :source "0" :target "4"}}
   {:data {:id "8" :source "4" :target "3"}}])

(def graph-styles
  "Add cytoscape specific styles here."
  [{:selector "node"
    :style {:shape "hexagon"
            :background-color "aliceblue"
            :label "data(id)"}}])

(defn on-background-tap
  "Creates a new node when a user clicks on the background."
  [cy]
  (fn [evt]
    (when (= (.-cyTarget evt) cy)
      (let [new-node-id (-> evt .-cy .-_private .-elements .-length)
            x-pos (-> evt .-cyRenderedPosition .-x)
            y-pos (-> evt .-cyRenderedPosition .-y)]
        (.add cy (clj->js {:data {:id new-node-id}
                           :position {:x x-pos :y y-pos}}))))))

(defn cyto-load
  "Loads the cytoscape graph."
  [nodes layout container-id style]
  (let [cy (js/cytoscape (clj->js
                          {:container (.getElementById js/document container-id)
                           :elements nodes
                           :style style}))]
    (.on cy "tap" (on-background-tap cy))))

(defn graph-container
  "Reagent component that contains the div where the graph will be rendered."
  [container-id height width nodes-and-edges style]
  (r/create-class {:reagent-render (fn [_] [:div {:id container-id :style {:width width :height height}}])
                   :component-did-mount #(cyto-load nodes-and-edges "grid" container-id (clj->js style))}))

(defcard-rg graph
  [:div
   [graph-container "container" 300 300 graph-data graph-styles]])
