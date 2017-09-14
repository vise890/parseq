(ns parseq.parsers
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(def ^{:doc "A parser that always fails"} fail-p
  (pu/->fail-p "fail-p"))

(defn one-p
  [input]
  (match input
         [x] [x nil]
         [x & y] [x y]
         _ (pu/->failure "one-p fail")))

(defn ->one=-p [val]
  (fn [input]
    (match input
           [val & rsin] [val rsin]
           _ (pu/->failure "one=-p fail"))))
