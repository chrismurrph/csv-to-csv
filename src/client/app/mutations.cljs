(ns app.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.client.data-fetch :as df]
            [app.ui :as ui]))

;(defn missing-file? [state] (empty? (-> @state :child/by-id 0 :file-contents)))
(defn missing-file? [state] (empty? (get-in @state [:child/by-id 0 :file-contents])))

(defmethod m/mutate 'app/upload-file [{:keys [state] :as env} k {:keys []}]
  (when (missing-file? state)
    ; remote must be the value returned by data-fetch remote-load on your parsing environment.
    {:remote (df/remote-load env)
     :action (fn []
               ; Specify what you want to load as one or more calls to load-action (each call adds an item to load):
               (df/load-action state :remote-file ui/Child
                               ;{:target [:file-contents]}
                               )
               ; anything else you need to do for this transaction
               )}))

