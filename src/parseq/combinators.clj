(ns parseq.combinators
  "
  Generic combinators for parsers


  NOTE: in this ns, parsers are denoted with -p and combinators with -c suffixes
  "
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(defn return
  "A parser that does nothing and always succeeds.
   It returns the input unchanged and the supplied `v` as a result"
  [v]
  (fn [input] [v input]))

;; bind :: [Parser a, a -> Parser b] -> Parser b
(defn bind
  "This is just a monadic bind. A.k.a. >>="
  [p f]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] (pu/parse (f r)  rsin)
           (r :guard pu/failure?) r)))

(defn fmap
  "Applies function `f` to the result of `p`"
  [f p]
  (bind p #(return (f %))))

(defn or-c
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

(defn many-c
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

(defn merge-c
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
                               (merge results r))
               (f :guard pu/failure?) f)))))

(defn peek-c
  "Peeks with p (and fails if p fails). Does not consume any input"
  [p]
  (fn [input]
    (match (pu/parse p input)
           [r _rsin] [r input]
           (r :guard pu/failure?) r)))
