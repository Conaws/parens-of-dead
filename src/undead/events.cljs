(ns undead.events
  (:require
   [cljsjs.firebase]
   [cljs.spec :as s]
   [clojure.string :as string]
   [cljs.tools.reader.edn :as edn]
   [posh.core :as posh :refer [posh!]]
   [clairvoyant.core :as trace :include-macros true]
   [re-frame-tracer.core :refer [tracer]]
   [re-frame.core :as re-frame :refer [subscribe
                                       dispatch
                                       reg-event-db reg-fx
                                       reg-event-fx]]
   [undead.db :as db]
   [datascript.core :as d]))


(trace/trace-forms {:tracer (tracer :color "green")}



(reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))



(reg-event-db
 :write-to
 (fn [db [_ p v]]
   (assoc-in db p v)))


(reg-event-db
 :cleanup
 (fn [db [_ p]]
   (assoc-in db p [])))



(def timestamp
  js/firebase.database.ServerValue.TIMESTAMP)

(defn db-ref [path]
  (.ref (js/firebase.database) (string/join "/" path)))

(defn save [path value]
  (.set (db-ref path) value))

(reg-fx
 :save-to-firebase
 (fn [[path value]]
   (.set (db-ref path) value)))


(reg-event-fx
 :save
 (fn [{db :db} [_ path value]]
   {:db db 
    :save-to-firebase [path value]}))


(reg-fx
 :firebase/sign-in-with-popup
 (fn [_]
   (do
     (.signInWithPopup
      (js/firebase.auth.)
      (js/firebase.auth.GoogleAuthProvider.)))))


(reg-event-fx
 :sign-in-with-popup
 (fn [_ _]
   {:firebase/sign-in-with-popup nil}))


(reg-event-db
 :load-user
 (fn [db [_ user-obj]]
   (assoc db :user user-obj)))


(reg-fx
 :firebase/signout
 (fn [_ _]
   (.signOut (js/firebase.auth))))



(re-frame/reg-event-fx
 :logout
 (fn [{db :db} _]
   {:firebase/signout nil
    :db (assoc db :user nil :conn nil)}))


(reg-fx
 :firebase/on-auth
 (fn on-auth [_ _]
   (.onAuthStateChanged
    (js/firebase.auth)
    (fn auth-state-changed [user-obj]
      (let [uid (.-uid user-obj)
            display-name (.-displayName user-obj)
            photo-url (.-photoURL user-obj)]
        (dispatch [:load-user {:photo-url photo-url
                               :display-name display-name
                               :uid uid
                               }])) )
    (fn auth-error [error]
      (js/console.log error)))))


(reg-event-fx
 :on-auth
 (fn [{db :db} _]
   {:db (assoc db :auth-triggered? true)
    :firebase/on-auth nil}))

(reg-fx
 :firebase/init
 (fn [_ _]
   (js/firebase.initializeApp
    #js {:apiKey "AIzaSyDEtDZa7Sikv7_-dFoh9N5EuEmGJqhyK9g"
         :authDomain "firescript-577a2.firebaseapp.com"
         :databaseURL "//firescript-577a2.firebaseio.com"
         :storageBucker ""})))


(reg-event-fx
 :init
 (fn [{db :db} _]
   {:db (assoc db :firebase-initialized? true)
    :firebase/init nil}))


(reg-event-fx
 :initialize-db
 (fn  [_ _]
   {:db db/default-db
    :firebase/init nil
    :dispatch [:on-auth]}))



(re-frame/reg-event-db
 :conn-from-firebase
 (fn [db [_ firebase-db]]
   ;; (js/console.log firebase-db)
   (let [
         new-conn (d/conn-from-db
                   firebase-db)]
     (do
       (js/console.log "Conn is conn" (d/conn? new-conn))
       (posh! new-conn))
     (assoc db :conn new-conn))))




;;; auth stuff

(reg-event-db
 :check-pword
 (fn [db [_ answr attmpt]]
   (if (= answr attmpt)
     (assoc db :authed :passed)
     (assoc db :authed :failed))))




(reg-fx
 :transact
 (fn transact-fx [[conn transaction]]
   (d/transact! conn transaction)))

(reg-event-fx
 :transact-atomic!
 (fn transact-fn [{db :db} [_ transaction]]
   (let [conn @(:atomic-conn db)]
     {:transact [conn transaction]})))


(reg-event-fx
 :save-conn-to-firebase
 (fn [{db :db} [_ path]]
   {:db db
    :save-to-firebase [path (pr-str @(:conn db))]}
   ))


(reg-event-fx
 :transact!
 (fn transact-fn [{db :db} [_ transaction]]
   (let [conn (:conn db)]
     {:transact [conn transaction]
      :dispatch-later [{:ms 100
                        :dispatch [:save-conn-to-firebase ["Poetry""db"]]}]}
     )))



(reg-event-db
 :reset-atomic-conn
 (fn reset-atomic [db [_ new-conn]]
   (let [_ (js/alert (type @new-conn))
         conn @(:atomic-conn db)
         _ (posh! new-conn)
         _ (reset! (:atomic-conn db) (d/reset-conn! conn new-conn))
         ]
     db)))


)






(s/check-asserts true)
