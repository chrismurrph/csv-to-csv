(ns app.domain)

;;
;; panels
;;
(def phone-list-panel 10)
(def phone-editor-panel 11)

;;
;; panel idents
;;
(defn phone-list-ident [id] [:phone-list/by-id id])
(defn phone-editor-ident [id] [:phone-editor/by-id id])

;;
;; entity idents
;;
(defn phone-ident [id] [:phone/by-id id])

;; Any alteration to this file will give:
;; Uncaught RangeError: Maximum call stack size exceeded
;; , when hot reloading

(def sleep-ms 300)

(defn sleep [msec]
  (js/setTimeout (fn []) msec))

(defn lag [f]
  (fn [& args]
    (sleep sleep-ms)
    (apply f args)))

(defn no-lag [f]
  (fn [& args]
    (apply f args)))
