(ns csv-to-csv.scratch
  (:require [csv-to-csv.utils :as u])
  ;(:import (clojure.lang Cons))
  )

(defn compose-1 [& fns]
  (fn [a & opts]
    (if opts
      (reduce (fn [x y]
                (apply y (if (seq? x) x (list x))))
              (cons a opts)
              (reverse fns))
      (reduce (fn [x y] (y x)) a (reverse fns)))))

(defn compose-passes [& fns]
  (fn [& args]
    (let [single? (= 1 (count args))
          use-apply? (not (and single? (coll? (first args))))
          use-apply? true
          start-with (if-not use-apply? (first args) args)]
      (->> (reverse fns)
           (reduce (fn [acc ele]
                     (if use-apply?
                       (list (apply ele acc))
                       (ele acc)))
                   start-with)
           first))))

(defn compose-3 [& fns]
  (fn [& args]
    (->> (reverse fns)
         (reduce (fn [acc ele]
                   (list (apply ele acc)))
                 args)
         first)))

(defn compose-2 [& fns]
  (fn [& args]
    (let [start-with (first args)]
      (->> (reverse fns)
           (reduce (fn [acc ele]
                     (list (apply ele acc)))
                   start-with)
           first))))

(defn x-1 []
  ((compose-3 rest reverse) [1 2 3 4]))

(defn x-2 []
  (= true ((compose-3 zero? #(mod % 8) +) 3 5 7 9)))

(defn x-3 []
  ((compose-3 +) 3 5 7 9))

(defn x-4 []
  ((compose-3 #(apply str %) take) 5 "hello world"))

(defn x-5 []
  ((compose-3
     #(apply str %)
     take)
    5
    "hello world"))