(ns undead.cards.cloze
  (:require [reagent.core    :as rx     ]
            [posh.core       :as rx-db  :refer [pull transact!]]
            [datascript.core :as mdb    ]
            [clojure.set     :as set    ]
            [cljs.reader]
            [clojure.string  :as str    ])
  (:require-macros
   [cljs.test :refer [testing is]]
   [reagent.ratom :refer [reaction]]
   [devcards.core   :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))



(def eggs "A story about an egg")
(def egge "A story about an egg")


(defonce textstate (rx/atom {:text egge}))


(defn split-sentences [text]
  (re-seq #".*[,?.]" text))




(defn nextelt
  "Given two characters, the previous row, and a row we are
  building, determine out the next element for this row."
  [char1 char2 prevrow thisrow position]
  (if (= char1 char2)
    (prevrow (- position 1))
    (+ 1 (min
          (prevrow (- position 1))
          (prevrow position)
          (last thisrow))))
  )

(defn nextrow
  "Based on the next character from string1 and the whole of string2
  calculate the next row. Initially thisrow contains one number."
  [char1 str2 prevrow thisrow]
  (let [char2 (first str2)
        position (count thisrow)]
    (if (= (count thisrow) (count prevrow))
      thisrow
      (recur
       char1
       (rest str2)
       prevrow
       (conj thisrow (nextelt char1 char2 prevrow thisrow position))))))

(defn levenshtein
  "Calculate the Levenshtein distance between two strings."
  ([str1 str2]
   (let [row0 (vec (map first (map vector (iterate inc 1) str2)))]
     (levenshtein 1 (vec (cons 0 row0)) str1 str2)))
  ([row-nr prevrow str1 str2]
   (let [next-row (nextrow (first str1) str2 prevrow (vector row-nr))
         str1-remainder (.substring str1 1)]
     (if (= "" str1-remainder)
       (last next-row)
       (recur (inc row-nr) next-row str1-remainder str2))))
  )






(defn find-levenshtein
  "Takes a value a word and a set and returns a set of all the words that are within the levensthein diff of value to that word"
  [n w s]
  (filter #(>= n (levenshtein w %)) s))







(defn cloze-delete [text word]
  (let [r (str/replace text word "______")]
    (if (not= text r)
      [r word]
      [text])))



(def re-word  #"[\w|']*\b")

(defn rand-word [s]
  (let [wordseq (re-seq #"[\w|']*\b" s)
        c (rand-int (count wordseq))]
    (nth wordseq c)))





(declare setf find-levenshtein)



(defn n-leven [n word set]
  (take n (sort-by #(levenshtein word %) set)))

(deftest clozetest
  (testing "cloze-delete removes a word if its there, and replaces it with a _____"
    (is (= ["I'm a little ______" "teapot"] (cloze-delete "I'm a little teapot" "teapot" )))
    (is (= ["______" "I'm"] (cloze-delete "I'm" "I'm"))))


  (testing "we can split a sentence up by word, including hyphens"
    (is (= '("I" "am" "a" "little" "teapot")
           (re-seq #"\w*\b" "I am a little teapot")))
    (is (= 4
           (count (re-seq #"[\w|']*\b" "I'm a little teapot")))))

  (testing "take a random word out of a sentence"
    (is
     (set/intersection (set (re-seq #"[\w|']*\b]" "I'm a little teapot")) (set (rand-word "I'm a little teapot")))))

  (testing "levensthein distance"
    (is (= 0 (levenshtein "this" "this")))
    (is (= 1 (levenshtein "this" "This")))
    (is (= 1 (levenshtein "this" "his")))
    (is (= 1 (levenshtein "this" "thiss")))
    (is (= 2 (levenshtein "this" "that"))))

  (testing "given a set of words, find all the words less than space away"
    (is  (= '("The" "they" "then" "the") (find-levenshtein 1 "the" setf))))
  (testing "get the 5 words from a set that are closest to this one -- in order of distance"
    (is  (not= '("The" "they" "then" "the" "see" "are") (n-leven 6 "the" setf)))
    (is  (= '("the" "The" "they" "then" "see" "are") (n-leven 6 "the" setf)))
    (is  (= (set '("The" "they" "then" "the" "see" "are")) (set (n-leven 6 "the" setf)))))
  )







  (defcard-doc
    "#Step 2:
Grab a random word from a sentence
##2a
use a regex that splits the sentence into a sequence of words (array will do)"
    (dc/mkdn-pprint-source re-word)
    "##2b
count the words in the sentence, take a random number between 1 and n, and take the word at that location"   
    (dc/mkdn-pprint-source rand-word))


(defn cloze-string [text]
  (fn []
    (let [tvec (->> (split-sentences text)
                    (map #(cloze-delete % (rand-word %)))
                    (take 5))]
      [:div
       (for [[s w] tvec]
         ^{:key s} [:div
                    [:h1 s]
                    [:button w]])])))






(declare n-leven)


(defn cloze-string2 [text]
  (fn []
    (let [tvec (->> (split-sentences text)
                    (map #(cloze-delete % (rand-word %)))
                    (take 5))
          words (set (re-seq re-word text))]
      [:div
       (for [[s w] tvec
             :let [choices (-> (take 5 words)
                               (conj w)
                               sort)]]
         ^{:key s} [:div
                    [:h1 s]
                    (for [c choices]
                      [:button c])])])))

(defcard-rg fill-in-blank1
  "Only taking random words"
  [cloze-string2 egge])




(defn cloze-string3 [text]
  (fn [text]
    (let [tvec (->> (split-sentences text)
                    (map #(cloze-delete % (rand-word %)))
                    (take 5))
          words (set (re-seq re-word text))]
      [:div
       (for [[s w] tvec
             :let [choices (-> (n-leven 5 w words)
                               sort)]]
         ^{:key s} [:div
                    [:h1 s]
                    (for [c choices]
                      [:button c])])])))



(defcard-rg fill-in-blank2
  "Using n-leven"
  [cloze-string3 egge])





(defn cloze-test [text word choices]
  (fn []
    (let [[a b] (cloze-delete word text)
          choices (conj choices b)]
      ^{:key "a"}[:div 
                  [:div
                   a]
                  [:div
                   (for [c (sort choices)]
                     ^{:key c}[:button {
                                         :on-click #(if (= word c)
                                                      (js/alert "Win")
                                                      (js/alert "Lose")  )} c])]])))


;; (defcard-rg clozecard
;;   [cloze-test "I'm a little teapot" "I'm" ["Me" "You" "I are"]])


(mapcat #(filter even? %) [[1 1 2] [2 2] [1 1 1]])



;;==== determining similarities in strings






(defcard-rg cloze-w-leven
  "Testing Leven algo"
  [cloze-test "I'm a little teapot" "teapot" (find-levenshtein 3 "teapot" setf)])


;;; test levenshein seq




(n-leven 10 "False" setf)



;; ======= test a sentence

(defn cloze-random [text]
  (cloze-delete (rand-word text) text))

(cloze-random "Hey there Sanj, how you don")


;; === test cloze  


(defn cloze-sentence [text wordset n]
  "Testing Leven algo"
  (fn []
    (let [[sent word] (cloze-delete (rand-word text) text )
          choices (conj (find-levenshtein n word wordset) word)]
      ^{:key "a"} [cloze-test sent word choices])))

;; (defcard-rg test-rand
;;   "Demostrates problem of when the word exists within a sentence
;; ##current problems
;; * Need to be able to specify the number of options desired, and increase leventhein count until it includes just those
;; * _Need to take into account 's_ 
;; * Need to not replace text within word boundaries"

;;   [cloze-sentence "Those is all the stuff" setf 2])





(def ftext "Falsifiability is the capacity to prove something is not correct.

  Scientists make hypotheses and theories about their fields of study. At the start, they hope their hypothesis or theory is true but they and other scientists will use the scientific method to try and prove it false. Falsifiability (or refutability) means that a theory or hypothesis can be proved wrong because it failed a critical test or experiment.

  A famous example in the 20th century was the expedition led by Arthur Eddington to Principe Island in Africa in 1919 to record the positions of stars around the Sun during a solar eclipse. The observation of star positions showed that the apparent star positions close to the Sun were changed. In effect, the light passing the Sun was pulled towards the sun by gravitation. This confirmed predictions of gravitational lensing made by Albert Einstein in the general theory of relativity, published in 1915. Eddington's observations were considered to be the first solid evidence in favour of Einstein's theory. Had the observations resulted differently, this would have counted against Einstein's theory, and perhaps refuted it.

  Karl Popper had the opinion that only theories that are falsifiable are scientific.[1] Falsifiability is then a line between science and other kinds of knowledge: if it can be refuted, it is science; it if cannot, then it is not science. Many working scientists think Popper was right.

  Not everyone agreed with this: Pierre Duhem and Paul Feyerabend had different ideas. Feyerabend's Against method (1975) argued that there was no one scientific method. Instead, whatever works, works and anything goes. This is called epistemological anarchy.

  Duham's idea was more subtle. He thought that for any given set of observations there is a huge and uncountable number of explanations. According to Duhem, an experiment in physics is not just an observation, but an interpretation of observations by means of a theoretical framework. Furthermore, no matter how well one constructs one's experiment, it is impossible to subject an isolated single hypothesis to an experimental test. Instead, it is a whole interlocking group of hypotheses, background assumptions, and theories that is tested. This thesis has come to be known as holism. According to Duhem, it makes crucial experiments impossible.

  There are some other problems with falsification:

  Kurt Gödel showed that certain propositions inside a system of logic cannot be proved inside that system.
  Closely related to this is the fact that some statements are undecidable (This statement is false, see paradox). Undecidable statements cannot be falsified")



(def seqf (re-seq re-word ftext))

(def setf (set seqf))



(defn wordmap [text]
  (let [words (str/split text " ")]
    (reduce (fn [memo word]
              (assoc memo word (inc (memo word 0)))) {} words)))


(defn more-than-in-map [n m]
  (filter (fn [[_ v]] (< n v)) m))



(for [[a b] (more-than-in-map 3 (wordmap ftext))]
  {a b})


(set
 (map first
      (sort-by second >
               (more-than-in-map 1 (wordmap ftext)))))



(sort-by second >
         '([0 1] [0 0] [0 3] [0 2])
         )








;; ==== other ways of slicing up text



(defn sentences [text]
  (rest (map str/trim (str/split text #"(.*[\? |\. |\! ])"))))


(defn not-blank [setofstrings]
  (filter #(not (identical? ""  %)) setofstrings))


(defn uniq-words [text]
  (set (not-blank (str/split text #"\s|\?|\!|\,|\.|\:"))))





