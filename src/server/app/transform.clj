(ns app.transform
  (:require [clojure.string :as s]
            [app.user-provided :as user]
            [utils :as u]))

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

;;
;; In reality user will check ignores from the incoming headings. Every heading will either be
;; ignored or translated. So in UI this won't need to be called
;;
(defn check-all-ignoreds-exist [test-file? ignore-headings headings]
  (assert (set? ignore-headings))
  (assert (boolean? test-file?))
  (assert (coll? headings))
  (let [diff (clojure.set/difference ignore-headings (set headings))
        ;_ (println "DIFF" diff)
        okay? (or (= #{} diff) test-file?)]
    (assert okay? (str "These ignored headings don't exist, so don't need to be on ignored list: " diff))
    headings))

(defn organise-row [make-translated-f value-populated-headings row-num]
  (let [accepted-count (count value-populated-headings)]
    (if (zero? accepted-count)
      {:row-num row-num :accepted-count accepted-count}
      (->> value-populated-headings
           (mapv make-translated-f)
           ;u/probe-on
           (group-by #((complement nil?) (:cell/to %)))
           (map (fn [[k v]] (if k [:translateds v] [:not-translateds v])))
           (into {:row-num row-num :accepted-count accepted-count})))))

(defn overwritten-column? [freqs]
  (when (seq freqs)
    (let [max-freq (apply max (vals freqs))]
      (> max-freq 1))))

(defn check-dups [row]
  (let [tos (map :cell/to (:translateds row))
        bad-row? (-> tos
                     frequencies
                     overwritten-column?)]
    (when bad-row?
      (println (str "Duplicated column in row: " (:translateds row))))))

(defn blank? [value] (or (nil? value) (= "" value)))
;(defn only-non-letters? [value] (re-find #" *" value))
(defn every-char-special? [value] (every? #{\* \space} value))

;;
;; There are ignores for each from-heading.
;; So "E-mail 1 - Type" might have [blank? only-non-letters?]
;; Most would have these two as defaults.
;; The theory is that each from-heading is given defaults, that can then
;; be altered by the user.
;; Obviously we only know the from-heading when given the file to import.
;;
(defn assemble-cell-ignores [from-heading]
  [blank? every-char-special?])

(defn not-rubbish? [[from-heading cell-value]]
  (let [preds (map complement (assemble-cell-ignores from-heading))]
    ((apply every-pred preds) cell-value)))

(defn make-translated [headings-from-to]
  (fn [[from-heading value]]
    (let [to-heading (get (into {} headings-from-to) from-heading)
          ;_ (println (str headings-from-to ", " from-heading))
          ]
      (if to-heading
        {:cell/from from-heading :cell/to to-heading :cell/value value}
        {:cell/from from-heading :cell/value value}))))

(defn row-reader-hof [test-file? ignore-headings headings-from-to]
  (let [_ (println "orig size: " (count headings-from-to))
        make-translated-f (make-translated headings-from-to)
        all-from-headings (mapv first headings-from-to)
        _ (println (str "all-from-headings: " all-from-headings))
        _ (println (str "ignore-headings: " ignore-headings))
        from-headings (remove ignore-headings all-from-headings)
        accepted-positions (utils/positions (set from-headings) all-from-headings)
        _ (println "accepted positions: " accepted-positions)
        [from-to-sz ignore-sz from-sz] (map count [headings-from-to ignore-headings from-headings])
        ]
    (assert (or test-file? (= (- from-to-sz ignore-sz) from-sz))
            (str "S/have ended up with " (- from-to-sz ignore-sz) ", but remove of " ignore-sz " didn't work as left with: " from-sz))
    (fn read-row [row-num row-data]
      ;(println (str "row size: " (count row-data)))
      ;(println (str "<" (seq row-data) ">, row-num: " row-num))
      (assert (= (count row-data) (count headings-from-to)) (str "headings: " (count headings-from-to) ", row-data: " (count row-data)))
      (let [accepted-row (map (vec row-data) accepted-positions)
            ;_ (println accepted-row)
            rows-sz (count accepted-row)
            headings-sz (count from-headings)]
        (u/assrt (= rows-sz headings-sz) (str rows-sz " not= " headings-sz))
        (let [value-populated-headings (->> accepted-row
                                            (map vector from-headings)
                                            (filter not-rubbish?))
              ;_ (println (str "row " row-num " reduced from " (count accepted-row) " to " (count value-populated-headings)))
              organised-row (organise-row make-translated-f value-populated-headings row-num)
              _ (check-dups organised-row)]
          organised-row)))))

;;
;; A useless translation configuration is one where the fields with anything in them are ignored.
;; So you end up with every delivered field being empty.
;; When described to the user it shows input lines that are ignored.
;;
(defn useless? [line]
  (and #_(-> line :accepted-count pos?) (-> line :translateds empty?)))

(defn determine-type [line]
  (cond
    (:not-translateds line) :non-translateds
    (useless? line) :useless-line))

(defn find-problem [test-file? ignore-headings headings-from-to lines]
  (let [row-reader-f (row-reader-hof test-file? ignore-headings headings-from-to)]
    (->> lines
         (map-indexed vector)
         (map (fn [[idx row-data]] (row-reader-f idx row-data)))
         (filter #(or (:not-translateds %) (useless? %)))
         (map #(assoc % :problem-type (determine-type %)))
         ;(map #(nth lines (:row-num %)))
         first
         ;u/probe-on
         )))

(defn perfect-translations [target-headings]
  (into {} (map (juxt identity identity) target-headings)))

(defn string-lines->translated [{:keys [test-file?
                                        target-headings
                                        translations
                                        ignore-headings] :as config}]
  (fn [{:keys [headings lines]}]
    (assert (seq? lines) (u/assert-str "lines" lines))
    (let [translated-headings (map
                                (merge (perfect-translations target-headings) translations)
                                headings)
          headings-from-to (mapv vector headings translated-headings)
          ;_ (println (str "headings: " headings))
          _ (check-all-ignoreds-exist test-file? ignore-headings headings)]
      (let [problem (find-problem test-file? ignore-headings headings-from-to lines)]
        (if problem
          ["PROBLEM:" (desc-problem headings lines problem)]
          (let [headings (vec (remove nil? translated-headings))]
            {:headings headings
             :lines    (->> lines
                            (cons translated-headings)
                            transpose
                            (remove #(nil? (first %)))
                            transpose
                            (remove all-blank?)
                            (drop 1))}))))))

;;
;; Get what is at the idx and replicate it for each many-fns
;;
(defn add-fields-into-line [idx many-preds]
  (fn [line]
    (assert (vector? line))
    (let [field-value (nth line idx)
          new-field-values (mapv (fn [f] (when (f field-value) field-value)) many-preds)]
      (vec (u/replace-in line idx new-field-values)))))

(defn swap-field-in-line [idx f]
  (fn [line]
    (assert (vector? line))
    (let [field-value (nth line idx)
          new-field-value (f field-value)]
      (vec (u/replace-in line idx new-field-value)))))

;;
;; Get the values at the indexes, join them together using join-fn.
;; Replace in at the first index and remove at all subsequent indexes.
;;
(defn rationalize-line [indexes join-fn]
  (let [[idx & idxs] indexes]
    (fn [line]
      (assert (vector? line))
      (let [values (map #(nth line %) indexes)
            one-value (join-fn values)]
        (-> line
            (u/replace-in idx one-value)
            (u/remove-at-indexes idxs))))))

;;
;; Inner fn must take and return {:keys [headings lines]}
;; So this and others like it can be composed before string-lines->translated
;;
(defn one->many [one-from-heading many-to-headings many-fn-keys]
  (let [many-fns (mapv user/key->fn many-fn-keys)]
    (fn [{:keys [headings lines]}]
      (let [idx (u/index-of one-from-heading headings)
            line-changer-f (add-fields-into-line idx many-fns)
            new-headings (u/replace-in headings idx many-to-headings)]
        {:headings new-headings
         :lines    (map line-changer-f lines)}))))

;; [["POCity" "PORegion" "POPostalCode"] :customer.company/addr-line-2 :spaced]
(defn many->one [many-from-headings one-to-heading concat-key]
  (let [one-fn (user/key->fn concat-key)]
    (fn [{:keys [headings lines]}]
      (let [idxs (map #(u/index-of % headings) many-from-headings)
            _ (assert (every? #(or (pos? %) (zero? %)) idxs))
            line-changer-f (rationalize-line idxs one-fn)
            new-headings (-> headings
                             (u/replace-in (first idxs) one-to-heading)
                             (u/remove-at-indexes (next idxs)))]
        {:headings new-headings
         :lines    (map line-changer-f lines)}))))

;; [:change ["EmailAddress" :sent-to-email-addresses :changer/collection]]
(defn change [from-heading to-heading fn-key]
  (let [f (user/key->fn fn-key)]
    (fn [{:keys [headings lines]}]
      (let [idx (u/index-of from-heading headings)
            line-changer-f (swap-field-in-line idx f)
            new-headings (vec (u/replace-in headings idx to-heading))]
        {:headings new-headings
         :lines    (map line-changer-f lines)}))))

(defn select-heading-value [heading line-num {:keys [headings lines]}]
  (let [idx (u/index-of heading headings)
        line (-> lines
                 vec
                 (nth line-num))]
    (nth line idx)))

(defn distinct-heading-values [heading {:keys [headings lines]}]
  (assert (coll? headings) (u/assert-str "headings" headings))
  (let [idx (u/index-of heading headings)]
    (assert idx (str heading " can't be found in " headings))
    (->> lines
         (keep #(nth % idx))
         distinct)))

(defn first-line [heading {:keys [headings lines]} value]
  (let [idx (u/index-of heading headings)]
    (->> lines
         (filter #(= value (nth % idx)))
         first)))

(defn as-map [headings]
  "Allows to conveniently look at the results as maps"
  (fn [line]
    (zipmap headings line)))