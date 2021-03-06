(defproject parens-of-the-dead "0.1.0-SNAPSHOT"
  :description "zombie game in cljs and clj"
  :url ""
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gol.html"}
  :jvm-apts ["-XX:MaxPermSize-256m"]
  :main undead.system
  :dependencies [[org.clojure/clojure "1.9.0-alpha11"]
                 [org.clojure/clojurescript "1.9.225"]
                 [com.stuartsierra/component "0.3.1"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.1"]
                 [com.rpl/specter "0.12.0"]
                 [devcards "0.2.1-7"]
                 [cljsjs/jquery "2.2.2-0"]
                 [cljsjs/typeahead-bundle "0.11.1-1"]
                 [reagent "0.6.0-SNAPSHOT"]
                 [posh "0.3.5"]
                 [datascript "0.15.4"]
                 [re-frame "0.8.0"]
                 [re-com "0.8.3"]
                 [cljsjs/d3 "4.2.2-0"]
                 [enlive "1.1.6"]
                 [reanimated "0.5.0"]
                 [soda-ash "0.1.0-beta"]
                 [day8/re-frame-tracer "0.1.1-SNAPSHOT"]
                 [cljsjs/firebase "3.2.1-0"]
                 [keybind "2.0.0"]
                 [reagent-forms "0.5.27"]
                 [cljsjs/dragula "3.6.8-0"]
                 [org.clojars.frozenlock/reagent-table "0.1.3"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                  "target"
                  "resources/public/js/devcards"]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.4"]
                             [lein-figwheel "0.5.4-7"]]
                   :dependencies [[reloaded.repl "0.2.2"]]
                   :source-paths ["dev"]
                   :externs ["externs.js"]
                   :cljsbuild {:builds [{:id :main
                                         :source-paths ["src" "dev"]
                                         :figwheel true
                                         :compiler {:main undead.client
                                                    :asset-path "js/app"
                                                    :output-to  "resources/public/js/compiled/app.js"
                                                    :output-dir "resources/public/js/app"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true}}
                                        {:id "devcards"
                                         :source-paths ["src"]
                                         :figwheel {:devcards true} ;; <- note this
                                         :compiler {:main  undead.cards
                                                    :optimizations :none
                                                    :asset-path "js/devcards"
                                                    :output-to  "resources/public/js/compiled/devcards.js"
                                                    :output-dir "resources/public/js/devcards"
                                                    :source-map-timestamp true }}

                                        ]}
                   :figwheel {
                              :server-port 3909 
                              :css-dirs ["resources/public/css"]
                              }
                   }})
