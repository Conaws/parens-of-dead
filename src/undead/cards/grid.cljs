(ns undead.cards.grid
  (:require [cljs-time.core :refer [now]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [button v-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [e sample-nodes schema]])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(def datatom (r/atom (now)))

(defcard-rg tabledrawa
  [v-box
   :gap "10px"
   :children
   [[rc/datepicker-dropdown :model datatom
     :on-change #(reset! datatom %)]
    [rc/datepicker :model datatom
     :on-change #(reset! datatom %)]
    [rc/title :label "title"
     :level :level1
     :underline? true]
    [button :label "ho"]
    [rc/scroller
     :v-scroll :auto
     :height "300px"
     :width "55px"
     :child [:div (repeat 1000 "lorem ipsum dolar")]]]
   ]
  )











