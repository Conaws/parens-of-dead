(ns undead.cards.auto
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(defn home-render []
  [:div.ui-widget
   [:label {:for "tags"} "Programming Languages: "]
   [:input.tags]]
  )


(defcard-rg home
  [home-render])


(def tags 
  ["ActionScript"
   "AppleScript"
   "Asp"
   "BASIC"
   "C"
   "C++"
   "Clojure"
   "COBOL"
   "ColdFusion"
   "Erlang"
   "Fortran"
   "Groovy"
   "Haskell"
   "Java"
   "JavaScript"
   "Lisp"
   "Perl"
   "PHP"
   "Python"
   "Ruby"
   "Scala"
   "Scheme"])

(defn home-did-mount []
  (js/$ (fn []
          (.autocomplete (js/$ ".tags") (clj->js {:source tags})))))

(defn home []
  (r/create-class {:reagent-render home-render
                   :component-did-mount home-did-mount}))

(defcard-rg autohome
  [:div
   [home]
   [home]
   [home]])

(defn portlet-toggle []
  [:span.ui-icon.ui-icon-minusthick.portlet-toggle])

(defn portlet-header [title]
  [:div.portlet-header.ui-widget-header.ui-corner-all
   [portlet-toggle]
   title]
  )

(defn sortable-render []
    [:div {:style {:height "700px"}}
     [:div.column
      [:div.portlet.ui-widget.ui-widget-content.ui-helper-clearfix.ui-corner-all
       [portlet-header "Feeds"]
       [:div.portlet-content
        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit"]]
      [:div.portlet
       [portlet-toggle]
       [:div.portlet-header "News"]
       [:div.portlet-content "Lorem ipsum dolor sit amet, consectetuer adipiscing elit"]] ]

     [:div.column
      [:div.portlet
       [portlet-toggle]
       [portlet-header "Shopping"]
       [:div.portlet-content "Lorem ipsum dolor sit amet, consectetuer adipiscing elit"]] ] 

     [:div.column
      [:div.portlet
       [portlet-header "Links"]
       [portlet-toggle]
       [:div.portlet-content "Lorem ipsum dolor sit amet, consectetuer adipiscing elit"]]
      [:div.portlet
       [portlet-header "Images"]
       [portlet-toggle]
       [:div.portlet-content "Lorem ipsum dolor sit amet, consectetuer adipiscing elit"]] ]
     [:div.column]
     [:div.column]
     ])



(defcard-rg sortable-render
  [sortable-render])


(defn sortable-did-mount []
  (js/$ (fn []
          (.sortable (js/$ ".column") (clj->js {:connectWith ".column"
                                                :handle ".portlet-header"
                                                :cancel ".portlet-toggle"
                                                :placeholder "portlet-placeholder ui-corner-all"})))))



(defn sortable []
  (r/create-class {:reagent-render sortable-render
                   :component-did-mount sortable-did-mount}))

(defcard-rg sortable
  [sortable])

