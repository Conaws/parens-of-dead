(ns undead.cards.d3
  (:require [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]]
            [undead.subs :as subs :refer [qe e]]
            [re-com.core :as rc :refer [h-box md-circle-icon-button v-box popover-tooltip]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(declare cyto cyto2)

(def sampgraph
  [
   {:data {:id "a"}}
   {:data {:id "c"}}
   {:data {:id "b"}}
   {:data {:id "d"}}
   {:data {:id "f"}}
   {:data {:id "g"}}
   {:data {:id "e"}}
   {:data {:id "ac" :source "c" :target "d"}}
   {:data {:id "ac"
           :source "a"
           :target "c"}}
   {:data {:id "ab"
           :source "a"
           :target "b"}}
   {:data {:id "bc"
           :source "b"
           :target "c"}}])


(defcard-rg d3
  [:div
   [:button.btn.btn-default {:on-click #(cyto)}
    :circle]
   [:button.btn.btn-default {:on-click #(cyto2)}
    :breadthfirst
    ]

   [:div#cy.bblack
    {:style {:width "300px"

             :height "300px"}}]
   
   ]
  )


(defn by-id [id]
  (.getElementById js/document (name id))
  )

(defn cyto []
  (js/cytoscape (clj->js
                 {:container (by-id "cy")
                  :elements sampgraph

                  :style [
                          {:selector "node"
                           :style {"background-color" "#666"
                                   "label"  "data(id)"}
                           }
                          {:selector "edge"
                           :style {"background-color" "red"
                                   "label"  "data(id)"}}]

                  :layout {
                           :name "circle"
                           :animationDuration 160
                           :animate true
                           :rows 3
                           }
                  }
                 

                 )))

(defn cyto2 []
  (js/cytoscape (clj->js
                 {:container (by-id "cy")
                  :elements sampgraph
                  :style [{:selector "node"
                           :style {"background-color" "#666"
                                   "label"  "data(id)"}}]
                  :layout {
                           :name "breadthfirst"
                           :animationDuration 160
                           :animate true
                           :rows 3
                           }
                  }
                 )))

(cyto)
