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
                 [compojure "1.5.1"]]
  :profiles {:dev {:plugins []
                   :dependencies [[reloaded.repl "0.2.2"]]
                   :source-paths ["dev"]}})

