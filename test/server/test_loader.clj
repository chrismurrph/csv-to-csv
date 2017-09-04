(ns test-loader
  (:require [clojure.test :refer :all]
            [csv-to-csv.utils :as u]
            [csv-to-csv.loader :as l]
            [csv-to-csv.transform :as t]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

;;
;; Allows to incorporate the functions that are going to be done before translation, so can
;; test them without doing the translate.
;;
(defn test-pre-translate [take-only {:keys [import-file-name ignore-headings complex-translations] :as config}]
  (assert (pos? (count complex-translations)))
  (let [f-of-fns (l/collect-complex-translations complex-translations)]
    (->> import-file-name
         l/get-input-lines
         ;; 2 is enough for the headings and one line
         (take take-only)
         l/vectorize
         u/probe-off
         f-of-fns)))

(deftest xero-invoices-import
  (let [config (-> "invoices_import.edn" io/resource u/read-edn)
        translated (->> config
                        (test-pre-translate 2))]
    ;(println (-> res :headings))
    ;(println (-> res :lines first))
    (is (= (->> translated
                (t/select-heading-value :customer.company/addr-line-2 0))
           "North Wollongong, NSW, 2500"))
    (is (= (->> translated
                (t/select-heading-value :sent-to-email-addresses 0))
           '("cmts@cmts.com.au")))))

(defn test-import []
  (let [config (-> "test_import.edn" io/resource u/read-edn)]
    (l/translate config)))

(defn distinct-heading-values [heading]
  (let [config (-> "invoices_import.edn" io/resource u/read-edn)
        translateds (test-pre-translate 1000 config)
        heading-values (t/distinct-heading-values heading translateds)]
    heading-values))

(defn distinct-heading-lines [heading]
  (let [config (-> "invoices_import.edn" io/resource u/read-edn)
        translateds (test-pre-translate 1000 config)
        heading-line (partial t/first-line heading translateds)
        heading-values (t/distinct-heading-values heading translateds)]
    (->> heading-values
         (map heading-line)
         (u/pp 350))))

(defn distinct-companies []
  (distinct-heading-lines :customer.company/name))

(defn distinct-contacts []
  (distinct-heading-values :customer.contact/name))
