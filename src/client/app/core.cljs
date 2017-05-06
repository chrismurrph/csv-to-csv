(ns app.core
  (:require
    app.mutations
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    [om.next :as om]
    [untangled.client.impl.network :as net]
    [app.ui :as ui]))

#_(defonce app (atom (uc/new-untangled-client
                     :networking (net/make-untangled-network specific-url :global-error-callback (constantly nil))
                     :started-callback
                     (fn [{:keys [reconciler]}]
                       (df/load-data reconciler [{:imported-docs (om/get-query ui/ShowdownDocument)}
                                                 {:imported-logins (om/get-query ld/LoginDialog)}]
                                     :post-mutation 'fetch/init-state-loaded
                                     :refresh [:app/docs :app/login-info])))))

(defonce app (atom (uc/new-untangled-client
                     ;:networking (net/make-untangled-network specific-url :global-error-callback (constantly nil))
                     :started-callback (fn [{:keys [reconciler] :as app}]
                                         (println "started, just message")
                                         (df/load-data reconciler [{:all-numbers (om/get-query ui/PhoneDisplayRow)}]
                                                       :post-mutation 'app/upload-file
                                                       :refresh [:app/docs :app/login-info])
                                         #_(df/load app :all-numbers ui/PhoneDisplayRow {:target  [:screen/phone-list :tab :phone-numbers]
                                                                                       :refresh [:screen-type]})))))

