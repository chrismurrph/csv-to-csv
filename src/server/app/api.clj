(ns app.api
  (:require [om.next.server :as om]
            [om.next.impl.parser :as op]
            [taoensso.timbre :as timbre]
            [app.loader :as l]
            [untangled.client.logging :as log]))

(defn make-phone-number [id type num]
  {:db/id id :phone/type type :phone/number num})

(defonce server-state {:all-numbers [(make-phone-number 1 :home "555-1212")
                                     (make-phone-number 2 :home "555-1213")
                                     (make-phone-number 3 :home "555-1214")
                                     (make-phone-number 4 :home "555-1215")]})

(defn update-phone-number [id incoming-changes]
  (log/info "Server asked to updated phone " id " with changes: " incoming-changes)
  (swap! server-state update-in [:all-numbers (dec id)] merge incoming-changes)
  )

; The server queries are handled by returning a map with a :value key, which will be placed in the appropriate
; response format
(defn read-handler [{:keys [state]} k p]
  (log/info "SERVER query for " k)
  (case k
    ; we only have one "server" query...get all of the phone numbers in the database
    :all-numbers {:value (get server-state :all-numbers)}
    ;(assert false (str "Can't handle query for: " k))
    nil
    ))

;; Server-side mutation handling. We only care about one mutation
(defn write-handler [env k p]
  (log/info "SERVER mutation for " k " with params " p)
  (case k
    `f/commit-to-entity (let [updates (-> p :form/updates)]
                          (doseq [[[table id] changes] updates]
                            (case table
                              :phone/by-id (update-phone-number id changes)
                              (log/info "Server asked to update unknown entity " table))))
    nil))

; Om Next query parser. Calls read/write handlers with keywords from the query
;(def server-parser (om/parser {:read read-handler :mutate write-handler}))
