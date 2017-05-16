(ns app.ui
  (:require [om.next :as om :refer [defui]]
            [om.dom :as dom]
            [untangled.client.core :as uc]
            [untangled.client.routing :as r :refer [defrouter]]
            [untangled.client.impl.network :as un]
            [untangled.client.logging :as log]
            [untangled.ui.forms :as f]
            [untangled.ui.layout :as l]
            [untangled.ui.elements :as ele]
            [untangled.client.data-fetch :as df]
            [cljs.reader :refer [read-string]]
            [app.domain :as domain]
            [untangled.client.mutations :as m :refer [defmutation]]))

(defn field-with-label
  "A non-library helper function, written by you to help lay out your form."
  ([comp form kw-name label-text] (field-with-label comp form kw-name label-text nil))
  ([comp form kw-name label-text validation-message]
   (dom/div #js {:className (str "form-group" (if (f/invalid? form kw-name) " has-error" ""))}
            (dom/label #js {:className "col-sm-2" :htmlFor kw-name} label-text)
            ;; THE LIBRARY SUPPLIES f/form-field. Use it to render the actual field
            (dom/div #js {:className "col-sm-10"} (f/form-field comp form kw-name))
            (when (and validation-message (f/invalid? form kw-name))
              (dom/span #js {:className (str "col-sm-offset-2 col-sm-10" kw-name)} validation-message)))))

(defui ^:once PhoneForm
  static f/IForm
  (form-spec [this] [(f/id-field :db/id)
                     (f/text-input :phone/number)
                     (f/dropdown-input :phone/type [(f/option :home "Home") (f/option :work "Work")])])
  static om/IQuery
  (query [this] [:db/id :phone/type :phone/number f/form-key])
  static om/Ident
  (ident [this props] (domain/phone-ident (:db/id props)))
  Object
  (render [this]
    (let [form (om/props this)]
      (dom/div #js {:className "form-horizontal"}
               ; field-with-label is just a render-helper as covered in basic form documentation
               (field-with-label this form :phone/type "Phone type:")
               (field-with-label this form :phone/number "Number:")))))

(def ui-phone-form (om/factory PhoneForm))

(defn- set-number-to-edit [state-map phone-id]
  (assoc-in state-map [:screen/phone-editor :tab :number-to-edit] (domain/phone-ident phone-id)))

;;
;; Interesting that change state every time click, even when no theoretical need second/subsequent times
;;
(defn- initialize-form [state-map form-class form-ident]
  (update-in state-map form-ident #(f/build-form form-class %)))

(defmutation edit-phone
             "Om Mutation: Set up the given phone number to be editable in the
             phone form, and route the UI to the form."
             [{:keys [id]}]
             (action [{:keys [state]}]
                     (swap! state (fn [state-map]
                                    (-> state-map
                                        (initialize-form PhoneForm (domain/phone-ident id))
                                        (set-number-to-edit id)
                                        (r/update-routing-links {:route-params {}
                                                                 :handler      :route/phone-editor}))))))


(defui ^:once PhoneDisplayRow
  static om/IQuery
  (query [this] [:ui/fetch-state :db/id :phone/type :phone/number])
  static om/Ident
  (ident [this props] [:phone/by-id (:db/id props)])
  Object
  (render [this]
    (let [{:keys [db/id phone/type phone/number]} (om/props this)]
      (l/row {:onClick #(om/transact! this `[(m/edit-phone {:id ~id})
                                             :ui/react-key])}
             (l/col {:width 2} (name type)) (l/col {:width 2} number)))))

(def ui-phone-row (om/factory PhoneDisplayRow {:keyfn :db/id}))

(defui ^:once PhoneEditor
  static uc/InitialAppState
  ; make sure to include the :screen-type so the router can get the ident of this component
  (initial-state [cls params] {:screen-type :screen/phone-editor})
  static om/IQuery
  ; NOTE: the query is asking for :number-to-edit. The edit mutation will fill this in before routing here.
  (query [this] [f/form-root-key :screen-type {:number-to-edit (om/get-query PhoneForm)}])
  Object
  (render [this]
    (let [{:keys [number-to-edit]} (om/props this)
          ; dirty check is recursive and always up-to-date
          not-dirty?  (not (f/dirty? number-to-edit))
          ; validation is tri-state. Most fields are unchecked. Use pure functions to transform the
          ; form to a validated state to check validity of all fields
          valid?      (f/valid? (f/validate-fields number-to-edit))
          not-valid?  (not valid?)
          save        (fn [evt]
                        (when valid?
                          (om/transact! this `[(f/commit-to-entity {:form ~number-to-edit :remote true})
                                               (r/route-to {:handler :route/phone-list})
                                               ; ROUTING HAPPENS ELSEWHERE, make sure the UI for that router updates
                                               :main-ui-router])))
          cancel-edit (fn [evt]
                        (om/transact! this `[(f/reset-from-entity {:form-id ~(domain/phone-ident (:db/id number-to-edit))})
                                             (r/route-to {:handler :route/phone-list})
                                             ; ROUTING HAPPENS ELSEWHERE, make sure the UI for that router updates
                                             :main-ui-router]))]
      (dom/div nil
               (dom/h1 nil "Edit Phone Number")
               (when number-to-edit
                 (ui-phone-form number-to-edit))
               (l/row {}
                      (ele/ui-button {:onClick cancel-edit} "Cancel")
                      (ele/ui-button {:disabled (or not-valid? not-dirty?)
                                      :onClick  save} "Save"))))))

(defui ^:once PhoneList
  static om/IQuery
  (query [this] [:screen-type {:phone-numbers (om/get-query PhoneDisplayRow)}])
  static uc/InitialAppState
  ; make sure to include the :screen-type so the router can get the ident of this component
  (initial-state [this params] {:screen-type   :screen/phone-list
                                :phone-numbers []})
  Object
  (render [this]
    (let [{:keys [phone-numbers]} (om/props this)]
      (dom/div nil
               (dom/h1 nil "Phone Numbers (click a row to edit)")
               (l/row {} (l/col {:width 2} "Phone Type") (l/col {:width 2} "Phone Number"))
               ; Show a loading message while we're waiting for the network load
               ((domain/lag df/lazily-loaded) #(mapv ui-phone-row %) phone-numbers)))))

(defrouter TopLevelRouter :top-router
           ; Note the ident function works against the router children, so they must have a :screen-type data field
           (ident [this props] [(:screen-type props) :tab])
           :screen/phone-list PhoneList
           :screen/phone-editor PhoneEditor)

(def ui-top-router (om/factory TopLevelRouter))
