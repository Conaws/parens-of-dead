(ns undead.cards.keys
  (:require [reagent.core :as r]
            )
  (:require-macros [devcards.core :refer [defcard-rg]]))



(def ^:private MODS
  {"shift" :shift
   "ctrl" :ctrl "control" :ctrl
   "alt" :alt "option" :alt
   "win" :meta "cmd" :meta "super" :meta "meta" :meta
   ;; default modifier for OS X is cmd and for others is ctrl
   "defmod" (if (neg? (.indexOf js/navigator.userAgent "Mac OS X"))
              :ctrl :meta)})

(def ^:private KEYATTRS
  {:shift "shiftKey" :ctrl "ctrlKey" :alt "altKey" :meta "metaKey"
   :code "keyCode"})

(def ^:private DEFCHORD {:shift false :ctrl false :alt false :meta false})

(def ^:private KEYS
  (merge {"backspace" 8,
          "tab" 9,
          "enter" 13, "return" 13,
          "pause" 19,
          "caps" 20, "capslock" 20,
          "escape" 27, "esc" 27,
          "space" 32,
          "pgup" 33, "pageup" 33,
          "pgdown" 34, "pagedown" 34,
          "end" 35,
          "home" 36,
          "ins" 45, "insert" 45,
          "del" 46, "delete" 46,

          "left" 37,
          "up" 38,
          "right" 39,
          "down" 40,

          "*" 106,
          "+" 107, "plus" 107, "kpplus" 107,
          "kpminus" 109,
          ";" 186,
          "=" 187,
          "," 188,
          "-" 189, "minus" 189,
          "." 190,
          "/" 191,
          "`" 192,
          "[" 219,
          "\\" 220,
          "]" 221,
          "'" 222
          }

    ;; numpad
    (into {} (for [i (range 10)]
               [(str "num-" i) (+ 95 i)]))

    ;; top row 0-9
    (into {} (for [i (range 10)]
               [(str i) (+ 48 i)]))

    ;; f1-f24
    (into {} (for [i (range 1 25)]
               [(str "f" i) (+ 111 i)]))

    ;; alphabet
    (into {} (for [i (range 65 91)]
               [(.toLowerCase (js/String.fromCharCode i)) i]))))




(def ^:private KNOWN-KEYS
  (into {} (for [[k v] KEYS]
             [v k])))

;; Data

;; useful for use of meta keys
(defn e->chord [e]
  (into {} (for [[key attr] KEYATTRS]
             [key (aget e attr)])))



(defonce BINDINGS (atom {"a" {"a" #(js/alert "a")
                              "b" #(js/alert "a")
                              "c" #(js/alert "a")
                              "d" #(js/alert "a")}
                         "b"  {"b" #(js/alert "a")
                               "c" #(js/alert "a")
                               "d" #(js/alert "a")}}))


(def PRESSED (r/atom []))

(defn ctrl-spc? [m]
  (and (:ctrl m)
       (= "space" )))





(defn dispatcher! [bindings]
  (fn [e]
    (when (get KNOWN-KEYS (.-keyCode e))
      (let [pressed-key (get KNOWN-KEYS (:code (e->chord e)))]
        (if (= "space" pressed-key)
          (swap! PRESSED empty)
          (do
            (swap! PRESSED conj pressed-key)))))))


(defcard-rg keytest
  [:textarea ])

(defn helm [pressed bindings]
  (fn []
    [:div
     (pr-str @pressed)
     (pr-str @bindings)]
     ))

  
(defcard-rg presstest
  [helm PRESSED BINDINGS]
  PRESSED
  {:inspect-data true
   :history true})


(defonce bind-keypress-listener
  (js/addEventListener "keydown" (dispatcher! BINDINGS) false))






