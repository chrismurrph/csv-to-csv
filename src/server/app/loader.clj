(ns app.loader
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(def real-file-name-2 "google_export.csv")
(def real-file real-file-name-2)
(def test-import "test_import")
(def my-input-file-name test-import)

;; Ignores used here won't apply to other situations
(def test-file? (= my-input-file-name test-import))

(defn get-input-lines [file-name]
  (line-seq (io/reader (io/resource file-name)))
  )

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

(defn append-ending [x positions]
  (conj positions (count x)))

;;
;; More complex because we don't count commas within quotes
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
         (map (fn [[y z]] (subs x (inc y) z)))
         )))

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

(defn transpose [xss]
  (assert (= (count (first xss)) (count (second xss)) (count (#(nth % 2) xss))))
  (apply map vector xss))

(defn desc-problem [incoming-headings lines p]
  [(:problem-type p)
   (case (:problem-type p)
     :non-translateds (:not-translateds p)
     :useless-line (remove nil?
                           (map (fn [heading cell-data]
                                  (when (not= "" cell-data)
                                    [heading cell-data]))
                                incoming-headings
                                (nth lines (:row-num p))
                                )))])

;;
;; A completely blank input line, resulting in completely blank output line is not a problem,
;; because problems are those that can be fixed by changing translation and ignores.
;;
(defn all-blank? [row]
  (every? #(= % "") row)
  )

(def target-fastmail-headings
  #{"Title", "First Name", "Last Name", "Nick Name", "Company", "Department", "Job Title", "Business Street",
    "Business Street 2", "Business City", "Business State", "Business Postal Code", "Business Country",
    "Home Street", "Home Street 2", "Home City", "Home State", "Home Postal Code", "Home Country",
    "Other Street", "Other Street 2", "Other City", "Other State", "Other Postal Code", "Other Country",
    "Business Fax", "Business Phone", "Business Phone 2", "Home Phone", "Home Phone 2", "Mobile Phone",
    "Other Phone", "Pager", "Birthday", "E-mail Address", "E-mail 2 Address", "E-mail 3 Address",
    "Notes", "Web Page"})
(def target-test-headings
  #{"Name", "Given Name", #_"Additional Name", "Family Name"})
(def target-headings (if test-file? target-test-headings target-fastmail-headings))

;;
;; Here heading we actually get can be mapped to one of the doco headings
;;
(def perfect-translations (into {} (map (juxt identity identity) target-headings)))

(def test-file-translations {"Informal Name" "Name",
                             "First Name"    "Given Name",
                             ;"Slurr" "Additional Name",
                             "Surname"       "Family Name"})
(def real-file-translations {
                             "Given Name"                       "First Name"
                             "Additional Name"                  "Nick Name"
                             "Family Name"                      "Last Name"
                             "Name Prefix"                      "Title"
                             "Organization 1 - Name"            "Company"
                             "Organization 1 - Department"      "Department"
                             "Address 1 - Street"               "Home Street"
                             "Address 1 - City"                 "Home City"
                             "Address 1 - Region"               "Home State"
                             "Address 1 - Postal Code"          "Home Postal Code"
                             "Address 1 - Country"              "Home Country"
                             "E-mail 1 - Value"                 "E-mail Address"
                             "Address 1 - PO Box"               "Home Street 2"
                             "Phone 2 - Value"                  "Home Phone 2"
                             "E-mail 2 - Value"                 "E-mail 2 Address"
                             "Address 1 - Extended Address"     "Home Street 2"
                             "Organization 1 - Type"            "Business City"
                             "Phone 3 - Value"                  "Other Phone"
                             "Phone 4 - Type"                   "Business Phone"
                             "Organization 1 - Yomi Name"       "Business Street 2"
                             "Phone 4 - Value"                  "Business Phone"
                             "Phone 1 - Value"                  "Home Phone"
                             "Organization 1 - Job Description" "Business State"
                             "Name Suffix"                      "Job Title"
                             "Organization 1 - Location"        "Business City"
                             ;;=> "Website 1 - Value"                "Web Page"
                             })

(def translations (if test-file? test-file-translations real-file-translations))

(def heading-translations (merge perfect-translations translations))

(declare find-problem check-all-ignoreds-exist)
(defn translate [string-lines]
  (let [[headings-str & lines-strs] string-lines
        incoming-headings (mapv s/trim (s/split headings-str #","))
        translated-headings (map heading-translations incoming-headings)
        headings-from-to (mapv vector incoming-headings translated-headings)
        ;_ (println (str "headings: " headings))
        _ (check-all-ignoreds-exist incoming-headings)
        lines' (for [line-str lines-strs]
                 (split-by-comma false line-str))
        lines (join-short-lines (count headings-from-to) lines')]
    (let [problem (find-problem headings-from-to lines)]
      (if problem
        ["PROBLEM:" (desc-problem incoming-headings lines problem)]
        (->> lines
             (cons translated-headings)
             transpose
             (remove #(nil? (first %)))
             transpose
             (remove all-blank?)
             )))))

(defn x-1 []
  (->> my-input-file-name
       get-input-lines
       ;translate
       ;(write-to-file my-output-file-name)
       ))

