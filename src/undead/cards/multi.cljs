(ns undead.cards.multi
  (:require
   [posh.core :as posh :refer [posh!]]
   [cljs.pprint :refer [pprint]]
   [re-com.core :as rc :refer [v-box box input-text h-box]]
   [datascript.core :as d]
   [reagent-forms.core :refer [bind-fields]]
   [clojure.string :as str]
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

(defn value-of [event]
  (-> event .-target .-value))



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





(defn multi [{:keys [highlight-class item-class list-class options]}]
  (let [a (r/atom "")
        selected-index (r/atom -1)
        typeahead-hidden? (r/atom false)
        mouse-on-list? (r/atom false)
        selections (r/atom #{ "A" "B" })
        save! #(swap! selections conj %)
        ]
    (fn []
      (let [options  (if (clojure.string/blank? @a)
                       []
                       (filter
                        #(-> % (.toLowerCase %) (.indexOf @a) (> -1))
                        options))
            matching-options (filter (comp not (set @selections)) options)
            choose-selected #(if (and (not-empty matching-options)
                                       (> @selected-index -1))
                              (let [choice (nth matching-options @selected-index)]
                                (save! choice)
                                (reset! a ""))
                              (do
                                (save! @a)
                                (reset! a "")))

            ]
        [:div.bblack
         (when @selections
           (for [x @selections]
                            ^{:key x}[:button {:on-click #(swap! selections (fn [y] (remove #{x} y)))}(str x)]
                            ))
         [:span (pr-str @selected-index)]
         [:input
          {:value @a
           :on-change #(reset! a (-> % .-target .-value))
           :on-key-down #(do
                           (case (.-which %)
                             38 (do
                                  (.preventDefault %)
                                  (when-not (= @selected-index -1)
                                    (swap! selected-index dec)))
                             40 (do
                                  (.preventDefault %)
                                  (when-not (= @selected-index (dec (count matching-options)))
                                    (swap! selected-index inc)))
                             9  (choose-selected)
                             13 (choose-selected)
                             27 (do (reset! typeahead-hidden? true)
                                    (reset! selected-index -1))
                             "default"))}]

         [:ul {:style
               {:display (if (or (empty? matching-options) @typeahead-hidden?) :none :block) }
               :class list-class
               :on-mouse-enter #(reset! mouse-on-list? true)
               :on-mouse-leave #(reset! mouse-on-list? false)}
          (doall
           (map-indexed
            (fn [index result]
              [:li {:tab-index     index
                    :key           index
                    :class         (if (= @selected-index index) highlight-class item-class)
                    :on-mouse-over #(do
                                      (reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex"))))
                    :on-click      #(do
                                      (reset! a "")
                                       (save! result)
                                    )}
               result])
            matching-options))]])
       )))

(defcard-rg bbc
  [multi {:highlight-class "highlight"
          :options ["E" "F" "Ab""Abb" "bbbAaaa"]}]
  )



(defn multi2 []
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



(defcard-rg bc
  [multi2])


(def schema {:node/title        {:db/unique :db.unique/identity}
             :node/prototype-of        {:db/valueType   :db.type/ref
                                        :db/cardinality :db.cardinality/many}
             :node/similar-to        {:db/valueType   :db.type/ref
                                      :db/cardinality :db.cardinality/many}
             :set/attributes   {:db/cardinality :db.cardinality/many}
             :set/members          {:db/valueType   :db.type/ref
                                    :db/cardinality :db.cardinality/many}})

(defonce lconn2 (d/create-conn schema))
(posh! lconn2)
(d/transact! lconn2
             [{:node/title "A"
               :set/members [{:node/title "A1"}
                             {:node/title "A2"}
                             {:node/title "A3"}
                             {:node/title "A4"}
                             {:node/title "A5"}
                             {:node/title "A6"}
                             ]}
              {:node/title "B"
               :set/members [{:node/title "B1"}
                             {:node/title "B2"}
                             {:node/title "A3"}
                             {:node/title "A4"}
                             {:node/title "A5"}
                             {:node/title "A6"}
                             ]}])




(defn multi-drop1 [nodes]
  (let [nodes nodes
        selections (r/atom [])
        selection-id (r/atom nil)]
    (fn []
      (let [new-nodes (keep (fn [[e t ty]]
                              (if (not ((set @selections)
                                        e))
                                {:id e :label t
                                 :group ty}
                                )) @nodes)
            sorted-nodes (sort-by :group new-nodes)]
        [rc/v-box
         :children [[rc/h-box
                     :children [(map (fn [e]
                                       [:button
                                        {:on-click #(reset! selections
                                                            (vec (remove #{e} @selections ))
                                                            )}
                                        e
                                        ]
                                       )  @selections)
                                [rc/single-dropdown
                                 :choices sorted-nodes
                                 :placeholder "If this then that"
                                 :filter-box? true
                                 :width "200px"
                                 :model selection-id
                                 :on-change #(do
                                               (reset! selection-id nil)
                                               (swap! selections conj %))]]]]
         ]))))


(defn select-text-nodes []
  [multi-drop1
   (posh/q lconn2 '[:find ?e ?text ?pt
                    :where [?e :node/title ?text]
                    [?p :set/members ?e]
                    [?p :node/title ?pt]])])


(defcard-rg stest
  [select-text-nodes])

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



(defn render-typeahead []
  (let [typeahead-value (r/atom nil)]
    (fn []
      [:div
       [:h1 (pr-str @typeahead-value)]
       [:input.typeahead
        {:type :text
         :on-select #(reset! typeahead-value (-> % .-target .-value))
         :placeholder "States"}]])))



(defn typeahead-js []
  (r/create-class
   {:component-did-mount typeahead-mounted
    :reagent-render render-typeahead}))




(defcard-rg typeahead-home
  [typeahead-js states])



(defn friend-source [text]
  (filter
   #(-> % (.toLowerCase %) (.indexOf text) (> -1))
   ["Alice" "Alan" "Bob" "Beth" "Jim" "Jane" "Kim" "Rob" "Zoe"]))







#_(defn typeahead
  [{:keys [id
           data-source
           input-class
           list-class
           item-class
           highlight-class
           input-placeholder
           result-fn
           choice-fn
           clear-on-focus?]
         :as attrs
         :or {result-fn identity
              choice-fn identity
              clear-on-focus? true}}
   {:keys [save!]}]

  (let [input-val (r/atom "")
        typeahead-hidden? (r/atom true)
        mouse-on-list? (r/atom false)
        selected-index (r/atom -1)
        selections (r/atom [])
        choose-selected #(when (and (not-empty @selections) (> @selected-index -1))
                           (let [choice (nth @selections @selected-index)]
                             (save! id choice)
                             (choice-fn choice)
                             (reset! typeahead-hidden? true)))]
    (fn []
      [:div.bblack
       [:input {:type        :text
                :placeholder input-placeholder
                :class       input-class
                :value       @input-val
                :on-focus    #(when clear-on-focus? (save! id nil))
                :on-blur     #(when-not @mouse-on-list?
                                (reset! typeahead-hidden? true)
                                (reset! selected-index -1))
                :on-change   #(reset! input-val (-> % .-target .-value))
                ;; (when-let [value (str/trim (value-of %))]
                ;;                 (reset! selections (data-source (.toLowerCase value)))
                ;;                 (reset! typeahead-hidden? false)
                ;;                 (reset! selected-index -1))
                :on-key-down #(do
                                (case (.-which %)
                                  38 (do
                                       (.preventDefault %)
                                       (when-not (= @selected-index 0)
                                         (swap! selected-index dec)))
                                  40 (do
                                       (.preventDefault %)
                                       (when-not (= @selected-index (dec (count @selections)))
                                         (save! id (value-of %))
                                         (swap! selected-index inc)))
                                  9  (choose-selected)
                                  13 (choose-selected)
                                  27 (do (reset! typeahead-hidden? true)
                                         (reset! selected-index 0))
                                  "default"))}]
       [:h1
        (pr-str @input-val)
        (pr-str @selected-index)
        (pr-str @selections)]

       ])))








#_(defmethod init-field :typeahead
  [[type {:keys [id data-source input-class list-class item-class highlight-class input-placeholder result-fn choice-fn clear-on-focus?]
          :as attrs
          :or {result-fn identity
               choice-fn identity
               clear-on-focus? true}}] {:keys [doc get save!]}]
  (let [typeahead-hidden? (atom true)
        mouse-on-list? (atom false)
        selected-index (atom -1)
        selections (atom [])
        choose-selected #(when (and (not-empty @selections) (> @selected-index -1))
                           (let [choice (nth @selections @selected-index)]
                             (save! id choice)
                             (choice-fn choice)
                             (reset! typeahead-hidden? true)))]
    (render-element attrs doc
                    [type
                     [:input {:type        :text
                              :placeholder input-placeholder
                              :class       input-class
                              :value       (let [v (get id)]
                                             (if-not (iterable? v)
                                               v (first v)))
                              :on-focus    #(when clear-on-focus? (save! id nil))
                              :on-blur     #(when-not @mouse-on-list?
                                              (reset! typeahead-hidden? true)
                                              (reset! selected-index -1))
                              :on-change   #(when-let [value (trim (value-of %))]
                                              (reset! selections (data-source (.toLowerCase value)))
                                              (save! id (value-of %))
                                              (reset! typeahead-hidden? false)
                                              (reset! selected-index -1))
                              :on-key-down #(do
                                              (case (.-which %)
                                                38 (do
                                                     (.preventDefault %)
                                                     (when-not (= @selected-index 0)
                                                       (swap! selected-index dec)))
                                                40 (do
                                                     (.preventDefault %)
                                                     (when-not (= @selected-index (dec (count @selections)))
                                                       (save! id (value-of %))
                                                       (swap! selected-index inc)))
                                                9  (choose-selected)
                                                13 (choose-selected)
                                                27 (do (reset! typeahead-hidden? true)
                                                       (reset! selected-index 0))
                                                "default"))}]

                     [:ul {:style {:display (if (or (empty? @selections) @typeahead-hidden?) :none :block) }
                           :class list-class
                           :on-mouse-enter #(reset! mouse-on-list? true)
                           :on-mouse-leave #(reset! mouse-on-list? false)}
                      (doall
                       (map-indexed
                        (fn [index result]
                          [:li {:tab-index     index
                                :key           index
                                :class         (if (= @selected-index index) highlight-class item-class)
                                :on-mouse-over #(do
                                                  (reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex"))))
                                :on-click      #(do
                                                  (reset! typeahead-hidden? true)
                                                  (save! id result)
                                                  (choice-fn result))}
                           (result-fn result)])
                        @selections))]])))
