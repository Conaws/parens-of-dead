(ns undead.cards.thenvenn.day
  (:require
   [goog.i18n.DateTimeFormat :as dtf]
   [posh.core :as posh :refer [posh!]]
   [re-com.core :as rc]
   [cljs-time.core :as time :refer [now]]
   [keybind.core :as keys]
   [cljs.pprint :refer [pprint]]
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



(def num->key
  {8  :backspace
   32 :space
   13 :enter
   38 :up
   16 :shift
   40 :down
   37 :left
   39 :right})

(defn e->key
  "Gets the key from a key event."
  {:possible-output `{:key :enter, :modifiers #{:alt}}}
  [e]
  (let [n (or (.-keyCode e) (.-which e))]
    {:key (get num->key n n)
     :modifiers (disj (hash-set (when (.-altKey e) :alt)) nil)}))


(def sdown (r/atom false))


(def bind-keydown-listener
  (js/addEventListener "keydown"
                       (fn [e] (condp = (:key (e->key e))
                                 16 (pprint "aaaa   ")
                                 :shift (reset! sdown true)
                                 :else
                                 ))
                       false
                       ))
(def bind-keyup-listener
  (js/addEventListener "keyup"
                       (fn [e] (condp = (:key (e->key e))
                                 16 (pprint "aaaa   ")
                                 :shift (reset! sdown false)
                                 :else))
                       false
                       ))


(deftest  time-test
  (testing "aaa"
    (is (= 1 1))))

(defonce active-atom (r/atom #{4 5 9}))
(defonce mdown-atom (r/atom false))

(defn grid-test [active-atom sdown]
  (let [mdown (r/atom false)]
    (fn [active-atom sdown]
      (let [k' @active-atom
            items (range 100)]
        [:table
         {:on-mouse-down #(reset! mdown true)
          :on-mouse-up #(reset! mdown false)
          :on-mouse-leave #(reset! mdown false)
         
          }
         [:thead (pr-str @sdown)]
         [:tbody
          (for [j (partition 10 items)]
            ^{:key (str "a" j)}
            [:tr
             (for [k j]
               ^{:key (str "cell" j k)}
               [:td.block
                {:style {:background-color
                         (if (k' k)
                           "blue"
                           "white")}
                 :on-click #(if (k' k)
                             (swap! active-atom clojure.set/difference #{k})
                             (swap! active-atom conj k))
                 :on-mouse-enter #(when @mdown
                                    (if @sdown
                                      (swap! active-atom clojure.set/difference #{k})
                                      (swap! active-atom conj k)))}
                ;; (str  j' j) 
                ])]
            )]
         ]))))

(defcard-rg grid-card
  [grid-test active-atom sdown]
  active-atom
  {:inspect-data true
  :watch-atom true })


;;; The first question right now is -- should inheritance be different from
;;; membership?
;;; I'll use term ancestor for now -- although parent might be better
;;; I'll just have a different thing for blocks 


(def thenvenn-data
  [{:db/id -1
    :set/title "Priorities"}
   {:db/id -2
    :set/title "Completed"}
   {:db/id -3
    :set/title "Email"
    :set/ancestor #{-4}}
   {:db/id -4
    :set/title "Busywork"}
   {:db/id -5
    :set/title "Email Amanda Peyton"
    :set/ancestor #{-3 -1}
    :set/exclude #{-5}}
   {:db/id -6
    :set/title "Create a Reagent typeahead style autocomplete"
    :set/ancestor #{-1
                    -2
                    {:db/id -7
                     :set/title "Reagent"}
                    {:db/id -8
                     :set/title "Autocomplete"}
                    {:db/id -9
                     :set/title "Create"}
                    }}

   {:db/id -10
    :day/date (now)}
   ]
  )



(deftest time-tst
  (testing "time string"
    (is (= [10 25 2016] ((juxt time/month time/day time/year) (time/today))))))


#_(defn slider [attr conn id]
  (let [itm (pull conn '[*] id)]
    (fn [attr conn id]
      [:div
       [:input {:type "range"
                :style {:display "flex"}
                :name "start"
                :value (get @itm attr 0)
                :min 0
                :max 100
                :step 1
                :on-change (fn [e]
                             (do #_(js/alert
                                    (js/parseInt (-> e .-target .-value)))
                                 (d/transact! conn [{:db/id id attr
                                                     (js/parseInt (-> e .-target .-value))}])))}]])))

(defn block-input []
  (let [block-number (r/atom 1)]
    (fn []
      [:div#bso.flex
       [:div.y.flex
        [:input {:type "range"
                 :style {:display "flex"}
                 :name "start"
                 :value @block-number
                 :min 0
                 :max 100
                 :step 1
                 :on-change (fn [e]
                              (reset! block-number
                                      (js/parseInt (-> e .-target .-value))))}]] 
       [:div
        [:div (for [i (partition-all 10 (range @block-number))]
                ^{:key i}[:div (for [x i]
                                 ^{:key x}[:button x])])]]
       ])
    )
  )




(defcard-rg bblockk
  [block-input])






