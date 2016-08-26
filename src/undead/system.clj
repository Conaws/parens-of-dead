(ns undead.system
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]))
 
(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defn- start-server [handler port]
  (let [server (run-server app {:port port})]
    (println "now running on port 8080")
    server))

(defrecord ParensOfTheDead []
  component/Lifecycle
  (start [this]
    (assoc this :server (start-server app 9009)))
  (stop [this])
  )

(defn -main [& args]
  )



