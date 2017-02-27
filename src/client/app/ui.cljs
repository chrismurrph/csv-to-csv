(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :refer [InitialAppState initial-state]]))

(declare Root)

(defui ^:once Child
  static InitialAppState
  (initial-state [cls params] {:id 0 :label (:label params)})
  static om/IQuery
  (query [this] [:id :label])
  static om/Ident
  (ident [this props] [:child/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id label]} (om/props this)]
      (dom/p nil label))))

(def ui-child (om/factory Child))

(defui ^:once Root
  static InitialAppState
  (initial-state [cls params]
    {:child (initial-state Child {:label "Constructed Label"})})
  static om/IQuery
  (query [this] [:ui/react-key {:child (om/get-query Child)}])
  Object
  (render [this]
    (let [{:keys [child ui/react-key]} (om/props this)]
      (dom/div #js {:key react-key}
        (ui-child child)))))

