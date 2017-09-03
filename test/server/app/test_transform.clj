(ns app.test-transform
  (:require
    [clojure.test :refer :all]
    [app.transform :as t]
    [utils :as u]))

(deftest many-joined-into-first
  (is (= ["a, b, d" "c" "e"]
         ((t/rationalize-line [0 1 3] (partial clojure.string/join ", ")) ["a" "b" "c" "d" "e"]))))

(def vectorized-by-one->many
  {:headings [:customer.company/name :customer.contact/name "EmailAddress" "POAddressLine1"
              "POAddressLine2" "POAddressLine3" "POAddressLine4" "POCity"
              "PORegion" "POPostalCode" "POCountry" "InvoiceNumber" "Reference" "InvoiceDate"
              "DueDate" "PlannedDate" "Total" "TaxTotal" "InvoiceAmountPaid" "InvoiceAmountDue"
              "InventoryItemCode" "Description" "Quantity" "UnitAmount" "Discount" "LineAmount"
              "AccountCode" "TaxType" "TaxAmount" "TrackingName1" "TrackingOption1" "TrackingName2"
              "TrackingOption2" "Currency" "Type" "Sent" "Status"],
   :lines    '(["CMTS NSW" nil "cmts@cmts.com.au" "1/30 Ralph Black Drive" "" "" "" "North Wollongong" "NSW" "2500"
                "Australia" "INV-4188" "" "1/08/2017" "6/08/2017" "" "1749.0000" "0.0000" "0.0000" "1749.0000" ""
                "BHP Mine Support" "1.0000" "1749.0000" "" "1749.0000" "200" "BAS Excluded" "0.0000" "" "" "" ""
                "AUD" "Sales invoice" "Sent" "Awaiting Payment"])})

(deftest three-together
  (let [many->one (apply t/many->one [["POCity" "PORegion" "POPostalCode"] :customer.company/addr-line-2 :spaced])]
    (is (= "North Wollongong, NSW, 2500"
           (t/select-heading-value :customer.company/addr-line-2 0 (many->one vectorized-by-one->many))))))
