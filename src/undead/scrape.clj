(ns undead.scrape
  (:require [net.cgrand.enlive-html :as html]
            [clojure.pprint :as pprint :refer [pprint]]))


;; hacker news

(def ^:dynamic *base-url* "https://news.ycombinator.com/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn hn-headlines []
  (map html/text (html/select (fetch-url *base-url*) [:td.title :a])))

(defn hn-points []
  (map html/text (html/select (fetch-url *base-url*) [:td.subtext html/first-child])))

(defn print-headlines-and-points []
  (doseq [line (map #(str %1 " (" %2 ")") (hn-headlines) (hn-points))]
    (println line)))


;; https://github.com/swannodette/enlive-tutorial


(defn pp [x]
  (pprint (map html/text x)))

(defn ppp [x]
  (pprint (map html/text x)))
;;; mit courses


(def all-mit-courses "https://ocw.mit.edu/courses/?utm_source=ocw-footer&utm_medium=link&utm_campaign=mclstudy")

(def courses (html/select
 (fetch-url all-mit-courses)
 [:h3.deptTitle :a]))

(def r1 ( take 4 (html/select
                 (fetch-url all-mit-courses)
                 [:table.courseList :tbody :td :> :a]
                 )))

#_(->> r1
 (map #(html/attr-values % :href))
 )


