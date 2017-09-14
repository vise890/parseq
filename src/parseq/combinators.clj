(ns parseq.combinators
  (:refer-clojure :exclude [or merge peek])
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(defn return
  "A parser that does nothing and always succeeds.
   It returns the input unchanged and the supplied `v` as a result"
  [v]
  (fn [input] [v input]))

(defn bind
  "This is just a monadic bind. A.k.a. >>=

   bind :: [Parser a, (a -> Parser b)] -> Parser b"
  [p f]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] (pu/parse (f r)  rsin)
           (r :guard pu/failure?) r)))

(defn fmap
  "Applies function `f` to the result of `p`

  fmap :: [Parser a, (a -> b)] -> Parser b"
  [f p]
  (bind p #(return (f %))))

(defn or
  "Tries `parsers` in sequence and returns the first one that succeeds. If they
  all fail, it fails."
  [& parsers]
  (fn [input]
    (loop [[p & ps :as parsers] parsers
           failures             []
           res                  nil]
      (if (empty? parsers)
        (if (pu/failure? res)
          (pu/->failure "or-c had no more parsers"
                        {:parsers-failures failures})
          res)
        (match (pu/parse p input)
               [r rsin] [r rsin]
               (f :guard pu/failure?) (recur ps (conj failures f) res))))))

(defn many
  "Parse `p` 0 or more times"
  ;; NOTE you could make this as a combo of `and-c` + `optional-c`, but may be
  ;;      worth leaving for perf?
  [p]
  (fn [input]
    (loop [results    []
           rest-input input]
      (match (pu/parse p rest-input)
             [r rsin] (recur (conj results r) rsin)
             (_ :guard pu/failure?) [results rest-input]))))

(defn merge
  "Applies `parsers` in order and then merges
  their results into one big fat map."
  [parsers]
  (fn [input]
    (loop [[p & ps :as pps] parsers
           rest-input       input
           results          {}]
      (if (empty? pps)
        [results rest-input]
        (match (pu/parse p rest-input)
               [r rsin] (recur ps
                               rsin
                               (clojure.core/merge results r))
               (f :guard pu/failure?) f)))))

(defn peek
  "Peeks with p (and fails if p fails). Does not consume any input"
  [p]
  (fn [input]
    (match (pu/parse p input)
           [r _rsin] [r input]
           (r :guard pu/failure?) r)))
