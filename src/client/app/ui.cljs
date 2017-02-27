(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :refer [InitialAppState initial-state]]))

(declare Root)

(defui ^:once Child
  static InitialAppState
  (initial-state [cls params] {:id 0 :file-contents (:file-contents params)})
  static om/IQuery
  (query [this] [:id :file-contents])
  static om/Ident
  (ident [this props] [:child/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id file-contents]} (om/props this)]
      (dom/div nil
               (dom/button #js {:onClick #(om/transact! this '[(app/upload-file)])} "Get File")
               (dom/p nil (str "input file size: " (count file-contents)))))))

(def ui-child (om/factory Child))

(defui ^:once Root
  static InitialAppState
  (initial-state [cls params]
    {:child (initial-state Child {:file-contents ""})})
  static om/IQuery
  (query [this] [:ui/react-key {:child (om/get-query Child)}])
  Object
  (render [this]
    (let [{:keys [child ui/react-key]} (om/props this)]
      (dom/div #js {:key react-key}
               (ui-child child)))))

