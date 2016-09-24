(ns undead.cards.grid
  (:require [cljs-time.core :refer [now]]
            [datascript.core :as d]
            [posh.core :as posh :refer [posh!]]
            [re-com.core :as rc :refer [button v-box]]
            [reagent.core :as r]
            [undead.subs :as subs :refer [e conn]]
            [clojure.set :as set]
            )
  (:require-macros [devcards.core :refer [defcard-rg]]))

(def datatom (r/atom (now)))

(defonce newconn conn)
(posh! newconn)

(def grid-transaction
  [{:db/id 1
    :node/type :set
    :node/title "Investors"
    :set/members [{:db/id -2
                   :node/title "Peter Thiel"}
                  {:db/id -3
                   :node/title "Elon Musk"}]}
   {:db/id -4
    :node/type :set
    :node/title "Contacted"
    :set/members [[:node/title "Peter Thiel"]
                  [:node/title "Elon Musk"]
                  ]}
   {:db/id -5
    :node/type :set
    :node/title "Scheduled"
    :set/members [[:node/title "Peter Thiel"]
                
                  ]}
   {:db/id -6
    :node/type :set
    :node/title "Investor Sets"
    :set/members [-5 -4 -7]
    }
   {:db/id -7
    :node/type :set
    :node/title "Pitched"
    :set/members [
                  ]}])


(d/transact! newconn grid-transaction)




(defn x-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))]
    (fn [props]
      [:input (merge props
                     {:type "text"
                      :value @val
                      :auto-focus true
                      :on-blur save
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))
(defn addset [conn title ptitle]
  (d/transact! conn [{:db/id -1
                      :node/title title
                      :node/type :set
                      :set/_members [:node/title ptitle]}])
  )


;; (defn editable-th [conn node]
;;   (let [editing (r/atom false)
;;         title (:node/title node)]
;;     (fn []
;;       (if @editing
;;         [:th [x-input {:title title
;;                        :on-stop #(reset! editing false)
;;                        :on-save #(d/transact! conn
;;                                               [[:db/add (:db/id node)
;;                                                 :node/title %]]
;;                                               )}]]
;;         [:th
;;          {:on-double-click #(reset! editing true)}
;;          title]
;;         )
;;       )
;;     )

;;   )


(defn simple-table [conn xtitle ytitle]
  (let [xelems (posh/pull conn '[:node/title {:set/members ...}] [:node/title xtitle])
        ysets (posh/pull conn '[:node/title :node/type {:set/members ...}] [:node/title ytitle])
        newelem (r/atom false)
        newst (r/atom false)]
    (fn []
      [:div.gridtest
       [:table
        [:thead [:tr [:th xtitle][:th]
                 (for [m (:set/members @ysets)]
                   [:th (:node/title m)])
                 [:th

                  (if @newst 
                    [x-input {:title ""
                              :on-stop #(reset! newst false)
                              :on-save #(addset conn % ytitle) 
                              }]
                    [rc/button :label (str "New " ytitle)
                     :on-click #(reset! newst true)]
                    )

                  ]]]
        [:tbody
         (for [m (:set/members @xelems)]
           [:tr [:th (:node/title m)][:th]
            (for [s (:set/members @ysets)]
              (if (contains? (set (:set/members s)) m )
                [:td.active
                 {:on-click
                  #(d/transact! conn [[:db/retract (:db/id s) :set/members (:db/id m) ]])
                  
                  }"Y"]
                [:td
                 {:on-click
                  #(d/transact! conn [{:db/id (:db/id s) :set/members (:db/id m)}])
                  }"N"]))])
         [:tr [:td (if @newelem [x-input
                                {:title ""
                                 :on-stop #(reset! newelem false)
                                 :on-save #(do
                                             (d/transact! conn [{:db/id -1
                                                                 :node/title %
                                                                 :set/_members [:node/title xtitle]}])
                                             )}
                                 ]
                       [rc/button :label (str "New " xtitle )
                        :on-click #(reset! newelem true)])]
          [:td]]
         ]]
       ;; (pr-str @conn)
       ])))


(defn simple-sets [conn topgroup]
  (let [root (posh/pull conn '[:node/type :node/title {:set/members ...}] [:node/title topgroup])]
    (fn []
      [:div.nest
       [:ol
        (for [m (:set/members @root)]
          [:div
           [:label (:node/title m)]
           [:ol
            (for [m (:set/members m)]
              [:li
               [:label (:node/title m)]
               ])]])]
       ]
      )))

(defn testa []
  (fn []
    [rc/h-box
     :children [


                [v-box
                 :gap "10px"
                 :children
                 [[rc/datepicker-dropdown :model datatom
                   :on-change #(reset! datatom %)]
                  ;; [rc/datepicker :model datatom
                  ;;  :on-change #(reset! datatom %)]
                  ;; [rc/title :label "title"
                  ;;  :level :level1
                  ;;  :underline? true]
                  ;; [button :label (pr-str @newconn)]
                  [simple-table newconn "Investors" "Investor Sets"]

                  [simple-sets newconn "Investor Sets"]
                  ]
                 ]]]))
(defcard-rg tabledrawa
 [testa] 
  )











