(ns parseq.parsers
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(defn ->fail
  "Builds a parser that always fails"
  ([msg] (fn [input] (pu/->failure msg)))
  ([msg data] (fn [input] (pu/->failure msg data))))

(def
  ^{:doc "A parser that always fails"}
  fail
  (->fail "fail-p"))

(defn one
  "A parser that takes (any) one element"
  [input]
  (match input
         [x] [x nil]
         [x & y] [x y]
         _ (pu/->failure "one-p fail")))

(defn ->one=
  "Builds a parser that takes an element that equals `v`"
  [v]
  (fn [input]
    (match input
           [v & rsin] [v rsin]
           _ (pu/->failure "one=-p fail"))))

(defn one-satisfying
  "Builds a parser that takes an element that satisfies `predicate`"
  [predicate]
  (fn [input]
    (match input
           [(v :guard predicate) & rsin] [v rsin]
           _ (pu/->failure "one-satisfying fail"))))
