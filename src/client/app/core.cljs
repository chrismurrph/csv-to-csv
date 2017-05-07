(ns app.core
  (:require
    app.mutations
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    [om.next :as om]
    [untangled.client.impl.network :as net]
    [app.ui :as ui]))

(defonce app (atom (uc/new-untangled-client
                     :started-callback (fn [{:keys [reconciler] :as app}]
                                         (df/load app :all-numbers ui/PhoneDisplayRow {:target  [:screen/phone-list :tab :phone-numbers]
                                                                                       ;;
                                                                                       ;; Causes a problem, but is essential.
                                                                                       ;; Changing the source code here (*) twice gets past the problem
                                                                                       ;; (*) 1. comment refresh
                                                                                       ;;     2. uncomment refresh
                                                                                       ;;
                                                                                       :refresh [:screen-type]
                                                                                       })))))

