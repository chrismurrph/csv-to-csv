(ns app.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.client.data-fetch :as df]
            [app.ui :as ui]
            [app.domain :as domain]
            [untangled.client.routing :as r]
            [untangled.client.mutations :as m :refer [defmutation]]
            [untangled.ui.forms :as f]))

(enable-console-print!)

(defn- set-number-to-edit [state-map phone-id]
  (assoc-in state-map [:screen/phone-editor :tab :number-to-edit] (domain/phone-ident phone-id)))

(defn- initialize-form [state-map form-class form-ident]
  (update-in state-map form-ident #(f/build-form form-class %)))

(defmutation edit-phone
             "Om Mutation: Set up the given phone number to be editable in the
             phone form, and route the UI to the form."
             [{:keys [id]}]
             (action [{:keys [state]}]
                     (swap! state (fn [state-map]
                                    (-> state-map
                                        (initialize-form ui/PhoneForm (domain/phone-ident id))
                                        (set-number-to-edit id)
                                        (r/update-routing-links {:route-params {}
                                                                 :handler      :route/phone-editor}))))))
