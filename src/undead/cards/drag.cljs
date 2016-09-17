(ns undead.cards.drag
  (:require [reagent.core :as r]
            [posh.core :as posh :refer [posh! pull q]] 
            [datascript.core :as d])
  (:require-macros [devcards.core :refer [defcard-rg]]))




(defn home-render []
  [:div.ui-widget-content {:style {:width "150px" 
                                   :height "150px" 
                                   :padding "0.5em"}}
   [:p
    {:on-drag-end #(js/alert %)}
    "Drag me around"]])


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
  [:div.tall
   [draggable]
   [drop-area]])


(defn select-render []
  [:ol#selectable
   (for [n (range 20)]
     ^{:key n}[:li n])])




(defcard-rg select
  [select-render])



(defn- select-did-mount [this]
  (.selectable (js/$ (r/dom-node this))
               (clj->js {:start #(js/console.log "yo")
                         :selecting (fn [e ui] (js/console.log ui))})
               ))

(defn select []
  (r/create-class {:reagent-render      select-render
                   :component-did-mount select-did-mount}))


(defcard-rg selectable
  [select])




(defn select- [render-fn]
  (r/create-class {:reagent-render      render-fn
                   :component-did-mount select-did-mount}))


(defn drag- [f]
  (r/create-class {:reagent-render f
                   :component-did-mount draggable-did-mount}))



(defn select2-render []
  [:ol#selectable
   (for [n (range 20)]
     ^{:key n}[drag- (fn [] [:li n])])])



(defcard-rg selectable
  [select- select2-render])



(defcard-rg buttondrag
  [:div
   [:button.tall {:on-drag-enter #(js/alert "wooohhhh")}]
   [:button {:draggable true} "woah"]])

(def schema {:set/members {:db/valueType :db.type/ref
                             :db/cardinality :db.cardinality/many}
             :certainty/target  {:db/valueType :db.type/ref
                                 :db/cardinality :db.cardinality/one}
             :logic/not  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}
             :logic/then  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}
             :logic/if  {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}})


(def lconn (d/create-conn schema))
(posh! lconn)


(def sample-nodes2
  [{:db/id      1
    :node/title "All Men are Mortal"}
   {:db/id      2
    :node/title "Socrates is a Man"}
   {:db/id      3
    :node/title "Socrates is Mortal"}
   {:db/id      4
    :logic/type :not
    :node/title "Not All Men are Mortal"
    :logic/not 1}
   {:db/id       5
    :logic/type  :and
    :logic/title  "A and B"
    :set/members #{1 2}}
   {:db/id      6
    :logic/type :if-then
    :logic/title "If (A and B) then C"
    :logic/if   5
    :logic/then 3}
   {:db/id 7
    :logic/type :or
    :logic/title "(B or C)"
    :set/members #{2 3}}
   {:db/id 8
    :node/title "D"}
   {:db/id 9
    :logic/type :or
    :logic/title "(B or C) or A"
    :set/members #{1 7}}
   {:db/id 10
    :logic/type :if-then
    :logic/title "If ((B or C) or A) then D"
    :logic/if 9
    :logic/then 8}
   {:db/id 11
    :certainty/score 90
    :certainty/target 1}
   {:db/id 12
    :certainty/score 50
    :certainty/target 2}
   {:db/id 13
    :certainty/score 40
    :certainty/target 3}
   ])



