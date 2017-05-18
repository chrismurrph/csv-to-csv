(ns cljs.user
  (:require
    [app.core :refer [app]]
    [untangled.client.core :as core]
    [cljs.pprint :refer [pprint]]
    [devtools.core :as devtools]
    [untangled.client.logging :as log]
    [app.root :as root]))

(enable-console-print!)

(reset! app (core/mount @app root/Root "app"))

; use this from REPL to view bits of the application db
(defn log-app-state
  "Helper for logging the app-state, pass in top-level keywords from the app-state and it will print only those
  keys and their values."
  [& keywords]
  (pprint (let [app-state @(:reconciler @app)]
            (if (= 0 (count keywords))
              app-state
              (select-keys app-state keywords)))))

#_(defn missing? []
  (let [app-state (:reconciler @app)]
    (mut/missing-file? app-state)))

; Om/dev logging level
;(log/set-level :none)
