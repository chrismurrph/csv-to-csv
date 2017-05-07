(ns app.root
  (:require [om.next :as om :refer [defui]]
            [om.dom :as dom]
            [om.next :as om]
            [app.core :as core]
            [untangled.client.routing :as r]
            [untangled.client.core :as uc]
            [app.ui :as ui]))

(defui ^:once Root
       static om/IQuery
       (query [this] [:ui/react-key {:main-ui-router (om/get-query ui/TopLevelRouter)}])
       static uc/InitialAppState
       (initial-state [cls params]
                      ; merge the routing tree into the app state
                      (merge
                        {:main-ui-router (uc/get-initial-state ui/TopLevelRouter {})}
                        (r/routing-tree
                          (r/make-route :route/phone-list [(r/router-instruction :top-router [:screen/phone-list :tab])])
                          (r/make-route :route/phone-editor [(r/router-instruction :top-router [:screen/phone-editor :tab])]))))
       Object
       (render [this]
               (let [{:keys [ui/react-key main-ui-router]} (om/props this)]
                 (dom/div #js {:key react-key}
                          (ui/ui-top-router main-ui-router)))))


