(ns undead.cards.search
  (:require [cljs-time.core :refer [now]]
            [clojure.set :as set]
            [datascript.core :as d]
            [re-com.core :as rc :refer [v-box box h-box]]
            [reagent.core :as r]
            [clojure.string :as str])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [com.rpl.specter.macros :refer [select]]
   [devcards.core :refer [defcard-rg deftest]]
   [undead.subs :refer [deftrack]]))


(def test-db (d/db-with (d/empty-db)
                        [{:db/id -1 :name "Sven"}]
                        ))

(defcard-rg hey
  [:div (pr-str test-db)])

(def states
  ["Alabama" "Alaska" "Arizona" "Arkansas" "California"
   "Colorado" "Connecticut" "Delaware" "Florida" "Georgia" "Hawaii"
   "Idaho" "Illinois" "Indiana" "Iowa" "Kansas" "Kentucky" "Louisiana"
   "Maine" "Maryland" "Massachusetts" "Michigan" "Minnesota"
   "Mississippi" "Missouri" "Montana" "Nebraska" "Nevada" "New Hampshire"
   "New Jersey" "New Mexico" "New York" "North Carolina" "North Dakota"
   "Ohio" "Oklahoma" "Oregon" "Pennsylvania" "Rhode Island"
   "South Carolina" "South Dakota" "Tennessee" "Texas" "Utah" "Vermont"
   "Virginia" "Washington" "West Virginia" "Wisconsin" "Wyoming"])

(defn matcher [strs]
  (fn [text callback]
    (->> strs
         (filter #(str/includes? % text))
         (clj->js)
         (callback))))

(defn typeahead-mounted [this]
  (.typeahead (js/$ (r/dom-node this))
              (clj->js {:hint true
                        :highlight true
                        :minLength 1})
              (clj->js {:name "states"
                        :source (matcher states)})))




(def typeahead-value (r/atom nil))

(defn render-typeahead []
  [:input.typeahead
   {:type :text
    :on-select #(reset! typeahead-value (-> % .-target .-value))
    :placeholder "States"}])



(defn typeahead []
  (r/create-class
   {:component-did-mount typeahead-mounted
    :reagent-render render-typeahead}))


(defn home []
  [:div.flex.column
   (when-let [language @typeahead-value]
     [:label "selected: " language])
   [typeahead]])


(defcard-rg typeahead-home
  [home])
