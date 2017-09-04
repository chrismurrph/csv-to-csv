(ns test-user-provided
    (:require
      [clojure.test :refer :all]
      [csv-to-csv.user-provided :as u]))

(deftest just-a-person
  (is (= (u/person? "Seaweed Person")
         true)))

(deftest not-a-person-a-company
  (is (= (u/person? "Seaweed Pty Ltd")
         false)))

(deftest a-company
  (is (= (u/company? "Seaweed Pty Ltd")
         true)))