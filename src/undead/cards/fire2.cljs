(ns undead.cards.fire2
  
  (:require [cljsjs.firebase]
            [reagent.ratom :as ratom]
            [undead.db :as db]
            [re-frame.core :as re-frame]
            [cljs.spec :as s]
            [clairvoyant.core :as trace :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]
            [re-frame.core :refer [dispatch subscribe]]
            [cljs.tools.reader.edn :as edn]
            [re-com.core :as re-com]
            [posh.core :as posh :refer [posh!]]
            [reagent.core :as r]
            [clojure.string :as str]
            [goog.dom.forms :as forms]
            [datascript.core :as d])
  (:require-macros [devcards.core :refer
                    [deftest defcard-rg]]
                   [undead.subs :refer [deftrack]]
                   [cljs.test :refer [testing is]]))







(defn test-form [path]
  (let [p (r/atom "")
        ]
    (fn []
      [:div.flex.flex-v.flex-center
       [:input {:value @p
                :placeholder "The Test value"
                :on-change #(reset! p (-> % .-target .-value))}]
       [:button {:on-click #(dispatch [:save path @p])}
        "Add to c"]
       ])))

(defn auth-form []
  (let [p (r/atom "")
        authed (subscribe [:get :authed nil])
        pword (subscribe [:firebase-path ["Poetry" "password"]])]
    (fn []
      [:div.flex.flex-v.flex-center
       [:h1 (str @authed)]
       [:input {:value @p
                :placeholder "The Secret Code"
                :on-change #(reset! p (-> % .-target .-value))}]
       [:button {:on-click #(do
                              (dispatch [:check-pword @pword @p])
                              (reset! p ""))}
        "Add to c"]
       ])))



(defn cover-panel []
  [:div.cover.flex-center.flex-v
   [:h1 "Welcome to Roam"]
   [:button.btn-cover
    {:on-click #(dispatch [:sign-in-with-popup])}
    "Sign In"]])




(defn small-panel [uid]
  (let [upath (subscribe [:firebase-path ["users" (str uid) "db"]])]
    (fn []
      [:div (pr-str @upath)
       [:button {:on-click #(dispatch [:conn-from-firebase @upath])} "Load up"]
       ])))

(defn old-panel []
  (let [user (subscribe [:user-id])
        u (subscribe [:user])
        conn (subscribe [:get :conn #_(d/create-conn db/schema)])
        pword (subscribe [:firebase-path ["Poetry" "password"]])
        demo (subscribe [:firebase-path ["test"]])
       ]
    (fn []
      [:div "Hey " (:display-name @u)
       [:h4 (pr-str @pword)]
       [auth-form]
       [:h4 ]
       [:h1 (pr-str @conn)]

       [:button
        {:on-click #(dispatch [:sign-in-with-popup])}
        "Sign In"]
       [:button {:on-click #(dispatch [:logout])}
        "Logout"]
       (when-let [u1 @user]
         [small-panel u1])
       [:h1 (pr-str @demo)]
       [test-form ["test"]]]
      )))


(declare all-q-atomic )

(defn q-view [atomic-conn]
  (let [all (all-q-atomic atomic-conn)]
    (fn [atomic-conn]
      [:div (pr-str @all)
       [:li (pr-str @atomic-conn)]
       [:button {:on-click
                 #(dispatch [:transact!
                             [{:node/title (str (rand-int 100) "Hoo")}]])
                 } "Hooo"]
       [:button {:on-click
                 #(d/transact! @atomic-conn
                              [{:node/title (str (rand-int 100) "Heyyyy")}]
                              )
                 } "Heyyy"]
       ]
      )))

(defcard-rg conn-card
  [:div "hey ho"])



(deftest firebase-test
  (testing "firebase-pul testl"
    (is (= "" @(subscribe [:firebase-path ["Poetry"]])))))

(defn old2-panel []
  (let [ac (subscribe [:atomic-conn])]
    [:div "Hey"
     [:h1 (pr-str @ac)]
     [:h1 "atomic-conn"]
     [:button {:on-click  #(dispatch [:reset-atomic-conn (d/conn-from-db db/test-db)])}
      "woah test db"]
     [q-view @ac]]))


;; (trace/trace-forms {:tracer (tracer :color "blue")}


(defn q-view2 [conn]
  (if (d/conn? conn)
    (let [a (posh/q '[:find ?e ?a ?v :where [?e ?a ?v]] conn)]
      [:div
       [:li (pr-str @a)]
       [:button {:on-click
                 #(dispatch [:transact!
                             [{:node/title (str (rand-int 100) "Hoo")}]])
                 } "Hooo"]
       [:button {:on-click
                 #(d/transact! conn
                               [{:node/title (str (rand-int 100) "Heyyyy")}]
                               )
                 } "Heyyy"]])
    [:h1 (pr-str conn)
     ]))


(defn panel []
  (let [ac (subscribe [:firebase-once-conn ["Poetry" "db"]])]
    (fn panel-view []
      [:div
       [q-view2 @ac]])))


;; )





(defn db-ref [path]
  (.ref (js/firebase.database) (str/join "/" path)))



(re-frame/reg-sub        ;; we can check if there is data
 :initialised?          ;; usage (subscribe [:initialised?])
 (fn  [db _]
   (not (empty? db))))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :user-id
 (fn [db _]
   (get-in db [:user :uid])))

(re-frame/reg-sub
 :user
 (fn [db _]
   (get-in db [:user])))






;; firebase-subscription can happen later - for now we can transact -save - load
;; which is highly inefficient, but what else to do...
;; (re-frame/reg-sub
;;  :firebase-db
;;  )


(defn count-tabs
  [string]
  (count (take-while #{\tab} string)))


(defn parsed [text]
  (->> (str/split text #"\n")
       (filter (comp not str/blank?))
       (map (juxt count-tabs str/trim))))


(defn transform-depthvec [nodefn edgefn sibling-collector nseq]
  (loop [result []
         s nseq]
    (let[[pdepth ptitle] (first s)
         [children siblings] (split-with #(< pdepth (first %)) (rest s))
         answer   (nodefn ptitle)
         answer
         (if (seq children)
           (edgefn answer (transform-depthvec nodefn edgefn sibling-collector children))
           answer)]
      (if (seq siblings)
        (recur (sibling-collector result answer) siblings)
        (sibling-collector result answer)))))



(defn title-parse [matcher title]
  (if (and (string? title) (str/starts-with? title matcher))
    (let [newtitle (str/replace-first title matcher "")]
      (str/trim newtitle))   )  )

(defn create-node-map [title]
  (or (when-let [t (title-parse "<" title)]
        {:input/type :parent
         :node/title t})
      (when-let [t (title-parse "+" title)]
        {:input/type :intersection
         :node/type :intersection
         :set/parents (vector t)})
      {:input/type :child
       :node/title title}))


;; this works fine IF, I can have deeply nested lookup,
;; and IF, it isn't too hard to remove the items
;; I have a feeling it won't work the way I want it to though


;;; realized why I can't pass the path down in intersection nodes-- because this is a recursive fn
;;; thus it moves from bottom up, not top down, the bottom ones don't even have
;;; anything they COULD add.. 

(defn connect-node [{title :node/title
                     type :input/type
                     parents :set/parents
                     :as node} children]
  (let [{c :child p :parent i :intersection} (group-by :input/type children)]
    (cond-> node
      c (assoc :set/members c)
      p (assoc :set/_members p)
      i (assoc :set/intersections
               (condp = type
                 :child (mapv #(update % :set/parents
                                       (fn [x] (conj x title))) i)
                 :parent (mapv #(update % :set/parents
                                        (fn [x] (conj x title))) i)
                 :intersection (mapv #(update % :set/parents (fn [x] (concat parents x))) i))
               ))))



(def depthvec->tree
  (partial transform-depthvec create-node-map connect-node conj))


(re-frame/reg-sub
 :parsed
 (fn [db _]
   (depthvec->tree (parsed (:text db "A\n\tB")))))






(re-frame/reg-sub
 :db
 (fn db-sub [db]
  db))


(re-frame/reg-sub-raw
 :firebase-path
 (fn [db [_ path]]
   (let [ref (db-ref path)
         query-token
         (.on ref "value"
              (fn [x]
                (dispatch [:write-to path (.val x)])))]

     (ratom/make-reaction
      (fn [] (get-in @db path []))
      :on-dispose #(do (.off ref))))))



(re-frame/reg-sub-raw
 :firebase-path
 (fn [db [_ path]]
   (let [ref (db-ref path)
         query-token
         (.on ref "value"
              (fn [x]
                (dispatch [:write-to path (.val x)])))]

     (ratom/make-reaction
      (fn [] (get-in @db path []))
      :on-dispose #(do (.off ref))))))



(re-frame/reg-sub
 :conn
 (fn [db _]
   (:conn db)))


(re-frame/reg-sub-raw
 :firebase-once-conn
 (fn [db [_ path]]
   (let [ref (db-ref path)
         query-token
         (.once ref "value"
                (fn recieved-db [x]
                  (let [firebase-db (edn/read-string {:readers d/data-readers} (.val x))
                        conn (if (s/valid? :roam.db/ds firebase-db)
                               (d/conn-from-db firebase-db)
                               (d/create-conn db/schema))]
                    (posh! conn)
                    (dispatch [:write-to [:conn] conn]))))]
     (ratom/make-reaction (fn [] (get @db :conn :loading))))))

(re-frame/reg-sub
 :atomic-conn
 (fn [db _]
   (:atomic-conn db)))

(re-frame/reg-sub
 :get
 (fn [db [_ k default-v]]
   (get db k default-v)))



(deftrack text-q [db]
  @(posh/q '[:find ?v ?text
             :where [?e :node/title ?v]
             [?e :node/text ?text]
             ]
           db))


#_(defn text-q [atomic-conn]
  (posh/q '[:find ?v ?text
            :where [?e :node/title ?v]
            [?e :node/text ?text]
            ]
          @atomic-conn))


(defn all-q-atomic [atomic-conn]
  (posh/q '[:find ?e ?a ?v
            :where [?e ?a ?v]]
          @atomic-conn))

(defn all-q [atomic-conn]
  (posh/q '[:find ?e ?a ?v
            :where [?e ?a ?v]]
          atomic-conn))



;; (re-frame/reg-sub
;;  :q
;;  (fn q-sub [db [_ query]]
;;    (posh/q query (:conn db))))

