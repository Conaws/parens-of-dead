(ns undead.cards.d3
  (:require [datascript.core :as d]
            [posh.core :as posh :refer [posh! pull q]]
            [undead.subs :as subs :refer [qe e]]
           [com.rpl.specter :as sp :refer [ALL filterer]] 
            [re-com.core :as rc :refer [h-box md-circle-icon-button v-box popover-tooltip]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [undead.cards.labels :as label]
            [undead.subs :as subs]
            [reagent.core :as r])
  (:require-macros [cljs.test  :refer [testing is]]
                   [com.rpl.specter.macros  :refer [select select-one
                                                    setval transform]]
                   [devcards.core :refer [defcard-rg]]))

(declare cyto-load)


(def components [{:data {:id "f"
                         :parent "d"}}
                 {:data {:id "ga" :source "g" :target "a"}}
                 {:data {:id "b"
                         :parent "f"}}
                 {:data {:id "fc" :source "f" :target "c"}}
                 {:data {:id "bc" :source "b" :target "c"}}
                 {:data {:id "ab" :source "a" :target "d"}}
                 {:data {:id "gb" :source "g" :target "b"}}
                 {:data {:id "bg" :source "b" :target "g"}}
                 {:data {:id "h" :parent "f"}}
                 {:data {:id "i" :parent "a"}}
                 {:data {:id "ih" :source "i" :target "h"}}
                 {:data {:id "g"
                         :parent "d"}}]
  )

(def sampgraph
  [
   {:data {:id "a"}}
   {:data {:id "c"}}
   {:data {:id "d"}}
   {:data {:id "e"}}
   {:data {:id "ac" :source "a" :target "c"}}
   {:data {:id "ad" :source "a" :target "d"}}
   {:data {:id "ae" :source "a" :target "e"}}
   {:data {:id "ed" :source "e" :target "d"}}
   ])


(defn cyto-render [id sampgraph]
  (fn [id sampgraph]
    [:div
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "circle" id )}  :circle]
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "random" id)}  :random]
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "grid" id )}  :grid]
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "dagre" id )}  :dagre]
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "cose-bilkent" id )}  :cose-bilkent]
     [:button.btn.btn-default {:on-click #(cyto-load sampgraph "breadthfirst" id)}  :breadthfirs]
     [:div.bblack
      {:id id
       :style {:width "300px"
               :height "300px"}}]]))


(defcard-rg d3
  [cyto-render "cx" (concat components sampgraph)] )


(defn by-id [id]
  (.getElementById js/document (name id))
  )


(defn cyto-load
 ([nodes layout id style]
  (let [cy (js/cytoscape (clj->js
                         {:container (by-id id)
                          :elements nodes
                          :style style
                          :layout {
                                   :name layout
                                   :animationDuration 60
                                   :animate true
                                   :padding 30
                                   :directed true
                                   :condense false
                                   :avoidOverlap true
                                   :rows 10
                                   }
                  ;        :autounselectify true
                          }))]
    ))


  ([nodes layout id]
   (let [cy 
         (js/cytoscape (clj->js
                        {:container (by-id id)
                         :elements nodes
                         :style [
                                 {:selector "edge"
                                  :style {:color "black"
                                          :width 3
                                          :line-color "#666"
                                          :target-arrow-shape "triangle"
                                          :target-arrow-color "black"
                                         }} 

                                 
                                 {:selector "node"
                                  :style {:background-color "#666"
                                          :text-halign "center"
                                          :text-valign "center"
                                          "label"  "data(id)"}
                                  }
                                 {:selector "$node > node"
                                  :style {
                                          :padding-top "10px"
                                          :padding-bottom "10px"
                                          :padding-left "10px"
                                          :padding-right "10px"
                                          :text-valign "top"
                                          :background-color "#bbb"

                                          "label"  "data(id)"}
                                  }

                                 {:selector ":selected"
                                  :style {:background-color "#666"
                                          "label"  "data(id)"}
                                  }

                                 {:selector ".faded"
                                  :style {:opacity 0.2}
                                  }
                                 ]

                         :layout {
                                  :name layout
                                  :animationDuration 460
                                  :animate true
                            ;      :padding 30
                                  :directed true
                                  :condense false
                                  :avoidOverlap true
                                  :avoidOverlapPadding 11
                                  :rows 4
                                  :cols 3
                                  }}))]
     (.on cy "tap" "node" #(do
                             (let [n (.-cyTarget %)
                                   neighborhood (-> n .neighborhood
                                                    (.add n))]
                               (-> cy .elements
                                   (.addClass "faded"))
                               (-> neighborhood (.removeClass "faded"))
                               )
                             (js/console.log
                              (->
                               % .-cyTarget .id))))
     )))



#_(defn cyto2-dm [nodes layout container]
  (js/cytoscape (clj->js
                 {:container container
                  :elements nodes
                  :style [
                          {:selector "node"
                           :style {"background-color" "#666"
                                   "label"  "data(id)"}
                           }
                          {:selector "edge"
                           :style {:color "red"
                                   "label"  "data(id)"}}]

                  :layout {
                           :name @layout
                           :animationDuration 160
                           :animate true
                           :directed true
                           :condense false
                           :avoidOverlap true
                           :fit true
                           :rows 4
                           :cols 4
                           }})))

(defn cyto2 [id nodes layout]
  (r/create-class {:reagent-render cyto-render
                   :component-did-mount #(cyto-load nodes layout id)}))



(defn cyto3 [id nodes]
  (let [layout (r/atom "grid")]
    (fn []
      [:div
       [cyto2 id nodes @layout]])))


(defcard-rg c3
  [cyto3 "cc" (concat components sampgraph) "dagre"])



(def nodes->chart
  (->> subs/sample-nodes
       (transform [(filterer #(not= :if (:node/type %))) ALL]
                  (fn [{n :db/id}]
                    {:data {:id (str n)}}))

       (transform [(filterer #(= :if (:node/type %))) ALL]
                  #(->> %
                        (transform sp/MAP-VALS str)
                        ((fn [{:keys [logic/if logic/then db/id]}]
                           {:data {:id id
                                   :source if
                                   :target then}}))))))

(defcard-rg aa
  [:div [cyto3 "akk"
         (->> subs/sample-nodes
              (transform [(filterer #(not= :if (:node/type %))) ALL]
                         (fn [{n :db/id}]
                           {:data {:id (str n)}}))

              (transform [(filterer #(= :if (:node/type %))) ALL]
                         #(->> %
                               (transform sp/MAP-VALS str)
                               ((fn [{:keys [logic/if logic/then db/id]}]
                                  {:data {:id id
                                          :source if
                                          :target then}})))))]])

(enable-console-print!)

(defn tolabel [n]
  (clj->js
   {:label n :width 144 :height 30}))

(let [g (-> (.-graphlib js/dagre)
            (.-Graph ))
      b (new g)]

(.setNode b "a" (tolabel "a"))
(.setNode b "c" (tolabel "c"))
(.setNode b "d" (tolabel "d"))
(.setEdge b "a" "b")
(.setEdge b "a" "c")
(.setDefaultEdgeLabel b (fn [_] {}))
(println (js->clj (.nodes b)))
(println (js->clj (.edges b)))
(println (identical? g b))
(println b)
(js/dagre.layout. b)
(println b)
)

#_(js* "
// Create a new directed graph 
var g = new dagre.graphlib.Graph();

// Set an object for the graph label
g.setGraph({});

// Default to assigning a new object as a label for each new edge.
g.setDefaultEdgeLabel(function() { return {}; });

// Add nodes to the graph. The first argument is the node id. The second is
// metadata about the node. In this case we're going to add labels to each of
// our nodes.
g.setNode("kspacey",    { label: "Kevin Spacey",  width: 144, height: 100 });
g.setNode("swilliams",  { label: "Saul Williams", width: 160, height: 100 });
g.setNode("bpitt",      { label: "Brad Pitt",     width: 108, height: 100 });
g.setNode("hford",      { label: "Harrison Ford", width: 168, height: 100 });
g.setNode("lwilson",    { label: "Luke Wilson",   width: 144, height: 100 });
g.setNode("kbacon",     { label: "Kevin Bacon",   width: 121, height: 100 });

// Add edges to the graph.
g.setEdge("kspacey",   "swilliams");
g.setEdge("swilliams", "kbacon");
g.setEdge("bpitt",     "kbacon");
g.setEdge("hford",     "lwilson");
g.setEdge("lwilson",   "kbacon");

console.log(g)
")


