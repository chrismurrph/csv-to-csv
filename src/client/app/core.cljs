(ns app.core
  (:require
    app.mutations
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    [om.next :as om]))

(defonce app (atom (uc/new-untangled-client)))

