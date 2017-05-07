(ns app.scratch)

(def matrix [[1 2 3][4 5 6][7 8 9]])

(defn x-1 []
  (matrix 0))

(defn x-2 []
  (matrix 0 1))

(defn x-3 []
  (get-in matrix [0 1]))

(defn getter [matrix]
  (fn [x y]
    (get-in matrix [x y])))

(defn x-4 []
  ((matrix 0) 1))

(defn x-5 []
  (let [g (getter matrix)]
    (g 0 1)))
