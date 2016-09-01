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
