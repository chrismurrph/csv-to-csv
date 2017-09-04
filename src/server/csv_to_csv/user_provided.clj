(ns csv-to-csv.user-provided
  (:require [clojure.string :as s]))

(defn proper-noun-like? [word]
  (and (Character/isUpperCase ^char (first word))
       (every? #(Character/isLowerCase ^char %) (rest word))))

(defn person? [txt]
  (let [names (s/split txt #" ")]
    (and
      (not (some #(= % "Ltd") names))
      (every? proper-noun-like? names))))

(def company? (complement person?))

(def key->fn
  {:concentrator/company company?
   :concentrator/person  person?
   :spaced               (partial clojure.string/join ", ")
   ;; Need to use list not vector. See assumption of replace-in
   :changer/collection   list})

