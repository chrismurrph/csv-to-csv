(ns csv-to-csv.loader
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [csv-to-csv.transform :as transform]
            [csv-to-csv.utils :as u]))

(defn get-file-as-string [file-name]
  (slurp (io/resource file-name)))

(defn get-input-lines [file-name]
  (line-seq (io/reader (io/resource file-name))))

(defn append-ending [x positions]
  (conj positions (count x)))

(defn finished? [{:keys [rest-line]}]
  (empty? rest-line))

(defn step [{:keys [rest-line curr-position comma-positions in-quote?]}]
  (let [curr-val (first rest-line)
        new-in-quote (if (= curr-val \")
                       (not in-quote?)
                       in-quote?)
        new-comma-positions (if (and (not in-quote?) (= curr-val \,))
                              (conj comma-positions curr-position)
                              comma-positions)]
    {:rest-line       (rest rest-line)
     :curr-position   (inc curr-position)
     :comma-positions new-comma-positions
     :in-quote?       new-in-quote}))

;;
;; More complex because we don't count commas within quotes
;; This is finally where returning a line hence mapv
;;
(defn split-by-comma [init-in-quote? x]
  (let [init-state {:rest-line       x
                    :curr-position   0
                    :comma-positions []
                    :in-quote?       init-in-quote?}]
    (->> (drop-while (complement finished?) (iterate step init-state))
         first
         :comma-positions
         (append-ending x)
         (into [-1])
         (partition 2 1)
         (mapv (fn [[y z]] (subs x (inc y) z))))))

;;
;; Normal concatenation won't work as the line that opened the quote was not known about when did
;; the second line, so the commas were not seen. Hence we do the splitting by commas operation again.
;; Only issue left here is that the joining cell, which is last of x and first of y, has a double quote
;; at its beginning and end.
;; Didn't solve as this joining column is ignored for file dealing with.
;;
(defn -concat [x y]
  (if (= \" (-> x last first))
    (concat (butlast x) (split-by-comma false (apply str (cons (str (last x) ", ") y))))
    (concat x y)))

(defn maybe-join [num-headings [x y]]
  (let [x-sz (count x)
        y-sz (count y)]
    (cond
      (= num-headings x-sz y-sz) y
      (and (< x-sz num-headings) (< y-sz num-headings)) (-concat x y)
      (= num-headings x-sz) x
      (= num-headings y-sz) y)))

;;
;; Even with the raw exported file (post dos2unix) some lines are too short, spilling onto the next line.
;; So here we recognise them and join them together
;;
(defn join-short-lines [expected-sz lines-strs]
  ;(println (str "counts: " (vec (remove #(= (second %) expected-sz) (map-indexed (fn [idx line] [idx (count line) (desc line)]) lines-strs)))))
  (->> (cons (repeat expected-sz "") lines-strs)
       (partition 2 1)
       (map (partial maybe-join expected-sz))))

;;
;; Get rid of the commas
;; We use terminology of vectorized to mean format of a vector then a list, where
;; the vector is of the headings and the list is of vectors, each representing a line.
;; Format we have chosen to deal with csv files internally.
;;
(defn vectorize [lines-from-file]
  (let [[headings-str & lines-strs] lines-from-file
        incoming-headings (mapv s/trim (s/split headings-str #","))
        lines' (for [line-str lines-strs]
                 (split-by-comma false line-str))
        lines (join-short-lines (count incoming-headings) lines')]
    ;(println (first lines))
    {:headings incoming-headings
     :lines    lines}))

;;
;; There are a lot of processor functions, each of which will take {:keys [headings lines]} and
;; return the same. So reduce over them to alter the state many times.
;;
(defn complex-translations-hof [processor-fns]
  ;(println (str "Num functions going to apply is " (count processor-fns)))
  (fn [{:keys [headings lines] :as st}]
    (reduce
      (fn [acc f]
        (let [{:keys [headings lines] :as vectorized} (f acc)]
          (assert (= (count headings) (-> lines first count))
                  (str "headings, first line, fn: " (count headings) ", " (-> lines first count) ", " f))
          vectorized))
      st
      processor-fns)))

(defn collect-complex-translations [translations]
  (->> translations
       (keep (fn [[name-key details]]
               ;(println name-key)
               (case name-key
                 :one->many (apply transform/one->many details)
                 :many->one (apply transform/many->one details)
                 :change (apply transform/change details)
                 nil)))
       complex-translations-hof))

(defn translate [{:keys [import-file-name ignore-headings complex-translations] :as config}]
  (assert (set? ignore-headings) (u/assert-str "ignore-headings" ignore-headings))
  (let [translations (collect-complex-translations complex-translations)
        transform (transform/string-lines->translated config)]
    (->> import-file-name
         get-input-lines
         vectorize
         translations
         transform)))

(defn google-import []
  (let [config (-> "google_import.edn" io/resource u/read-edn)]
    (translate config)))

(defn invoices-import []
  (let [config (-> "invoices_import.edn" io/resource u/read-edn)]
    (translate config)))
