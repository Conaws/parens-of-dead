(ns undead.cards.multi
  (:require
   [posh.core :as posh :refer [posh!]]
   [cljs.pprint :refer [pprint]]
   [re-com.core :as rc :refer [v-box box input-text h-box]]
   [datascript.core :as d]
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]])
  (:require-macros
   [cljs.test  :refer [testing is]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder style]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder :style style
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def todo-edit (with-meta todo-input
                 {:component-did-mount #(.focus (r/dom-node %))}))


(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox" :checked done
                        :on-change #(pprint "yaaa ")}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click #(pprint "yoooo ")}]]
       (when @editing
         [todo-edit {:class "edit" :title title
                     :on-save #(pprint (str  id %))
                     :on-stop #(reset! editing false)}])])))





(defn multi []
  (let [a (r/atom "")
        selections (r/atom ["A" "B"])]
    (fn []
      [:div.bblack
       (for [x @selections]
         ^{:key x}[:button {:on-click #(swap! selections (fn [y] (remove #{x} y)))}(str x)]
         )
       [todo-input {:style {:color "blue"
                            :right "-1" }
                    :on-save #(swap! selections conj %)}
        ]
       ])))

(defcard-rg bbc
  [multi]
  )

