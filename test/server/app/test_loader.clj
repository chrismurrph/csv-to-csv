(ns app.test-loader
  (:require [clojure.test :refer :all]
            [utils :as u]
            [app.loader :as l]
            [app.transform :as t]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

;;
;; Allows to incorporate the functions that are going to be done before translation, so can
;; test them without doing the translate.
;;
(defn test-pre-translate [{:keys [import-file-name ignore-headings complex-translations] :as config}]
  (assert (pos? (count complex-translations)))
  (let [f-of-fns (l/collect-complex-translations complex-translations)]
    (->> import-file-name
         l/get-input-lines
         ;; 2 is enough for the headings and one line
         (take 2)
         l/vectorize
         u/probe-off
         f-of-fns)))

(deftest xero-invoices-import
  (let [config (-> "invoices_import.edn" io/resource u/read-edn)
        res (->> config
                 test-pre-translate)]
    ;(println (-> res :headings))
    ;(println (-> res :lines first))
    (is (= (t/select-heading-value :customer.company/addr-line-2 0 res)
           "North Wollongong, NSW, 2500"))
    (is (= (t/select-heading-value :sent-to-email-addresses 0 res)
           '("cmts@cmts.com.au")))))
