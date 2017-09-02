(ns app.user-provided
  (:require [clojure.string :as s]))

(defn proper-noun-like? [word]
  (and (Character/isUpperCase ^char (first word))
       (every? #(Character/isLowerCase ^char %) (rest word))))

(defn person? [txt]
  (let [names (s/split txt #" ")]
    (every? proper-noun-like? names)))

(def company? (complement person?))

(def key->fn
  {:concentrator/company company?
   :concentrator/person  person?})

