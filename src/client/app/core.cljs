(ns app.core
  (:require
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    [om.next :as om]
    [untangled.client.impl.network :as net]
    [app.domain :as domain]
    [app.ui :as ui]))

(defonce app (atom (uc/new-untangled-client
                     :started-callback (fn [{:keys [reconciler] :as app}]
                                         (df/load app :all-numbers ui/PhoneDisplayRow {:target  [:screen/phone-list :tab :phone-numbers]
                                                                                       :refresh [:main-ui-router]})))))

