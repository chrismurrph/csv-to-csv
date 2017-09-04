(ns user
  (:require
    [clojure.pprint :refer (pprint)]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.tools.namespace.repl :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.stuartsierra.component :as component]))

(set-refresh-dirs "src/server" "dev/server" "test/server")

(defn restart
  "Stop the web server, refresh all namespace source code from disk, then restart the web server."
  []
  (refresh))