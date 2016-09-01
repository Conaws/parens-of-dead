(ns undead.cards.setdrag
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))




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





(defn- select-did-mount [this]
  (.selectable (js/$ (r/dom-node this))
               (clj->js {:start #(js/console.log "yo")
                         :selecting (fn [e ui] (js/console.log ui))})
               ))




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
