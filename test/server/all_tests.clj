(ns all-tests
  (:require [clojure.test :as test]))

(defn run []
  #_(test/run-all-tests)
  (test/run-tests
    'test-loader
    'test-transform
    'test-user-provided
    ))
