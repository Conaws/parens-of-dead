(ns undead.views
  (:require [re-frame.core :as re-frame
             :refer [reg-event-db reg-sub]]
            [undead.routes :as routes]))



(reg-event-db
 :set-active-panel
 (fn [db [_ panel-name]] 
   (assoc db :active-panel panel-name)))


(reg-sub
 :active-panel
 (fn [db]
   (:active-panel db)))



;; --------------------
(defn home-panel []
    (fn []
      [:div (str "Hello from Home" ". This is the Home Page.")
       [:div [:a {:href (routes/url-for :about)} "go to About Page"]]]))

(defn about-panel []
  (fn []
    [:div "This is the About Page."
     [:div [:a {:href (routes/url-for :home)} "go to Home Page"]]]))

;; --------------------
(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :about-panel [] [about-panel])
(defmethod panels :default [] [home-panel])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [:div
       [:h1 "Loaded smthn"]
       (panels @active-panel)])))

