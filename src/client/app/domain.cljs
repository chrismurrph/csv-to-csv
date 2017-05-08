(ns app.domain)

(defn phone-ident [id] [:phone/by-id id])

;; Any alteration to this file will give:
;; Uncaught RangeError: Maximum call stack size exceeded
;; , when hot reloading

(def sleep-ms 50)

(defn sleep [msec]
  (js/setTimeout (fn []) msec))

(defn lag [f]
  (fn [& args]
    (sleep sleep-ms)
    (apply f args)))

(defn no-lag [f]
  (fn [& args]
    (apply f args)))
