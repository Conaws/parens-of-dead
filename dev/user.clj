(ns user
  (:require [reloaded.repl :refer [reset stop system]]
            [undead.system]))

(reloaded.repl/set-init! #'undead.system/create-system)
