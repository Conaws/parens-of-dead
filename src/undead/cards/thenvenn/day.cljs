(ns undead.cards.thenvenn.day
  (:require
   [goog.i18n.DateTimeFormat :as dtf]
   [posh.core :as posh :refer [posh!]]
   [datascript.core :as d]
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]) )

(def format-map
  (let [f goog.i18n.DateTimeFormat.Format]
    {:FULL_DATE (.-FULL_DATE f)
     :FULL_DATETIME (.-FULL_DATETIME f)
     :FULL_TIME (.-FULL_TIME f)
     :LONG_DATE (.-LONG_DATE f)
     :LONG_DATETIME (.-LONG_DATETIME f)
     :LONG_TIME (.-LONG_TIME f)
     :MEDIUM_DATE (.-MEDIUM_DATE f)
     :MEDIUM_DATETIME (.-MEDIUM_DATETIME f)
     :MEDIUM_TIME (.-MEDIUM_TIME f)
     :SHORT_DATE (.-SHORT_DATE f)
     :SHORT_DATETIME (.-SHORT_DATETIME f)
     :SHORT_TIME (.-SHORT_TIME f)}))


(defn format-date*
  "Format a date using either the built-in goog.i18n.DateTimeFormat.Format enum
or a formatting string like \"dd MMMM yyyy\""
  [date-format date]
  (.format (goog.i18n.DateTimeFormat.
            (or (date-format format-map) date-format))
           (js/Date. date)))

(format-date* :SHORT_DATE (js/Date.))
(format-date* :MEDIUM_TIME (js/Date.))
(format-date* :SHORT_TIME (js/Date.))
(def short-date (partial format-date* :SHORT_DATETIME))
(def short-time (partial format-date* :SHORT_TIME))


(deftest  time-test
  (testing "aaa"
    (is (= 1 1))))

(def active-atom (r/atom [0 5]))

(defn grid-test [active-atom]
  (fn []
    (let [[j' k'] @active-atom]
      [:table
       (for [j (range 10)]
         ^{:key (str "a" j)}
         [:tr
          (for [k (range 10)]
            ^{:key (str "cell" j k)}
            [:td.block
                                     {:style {:background-color
                                              (if (and (= k k')
                                                       (= j j'))
                                                "blue"
                                                "white")}
                                      :on-click #(reset! active-atom [j k])}
             ;; (str  j' j) 
                                     ])]
         )
       ]))

  )

(defcard-rg grid-card
  [grid-test active-atom]
  active-atom
  {:inspect-data true
   :history true})
