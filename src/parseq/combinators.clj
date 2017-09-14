(ns parseq.combinators
  (:refer-clojure :exclude [or merge peek + *])
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(defn return
  "A parser that does nothing and always succeeds.
   It returns the input unchanged and the supplied `v` as a result."
  [v]
  (fn [input] [v input]))

(defn bind
  "This is just a monadic bind. A.k.a. >>=

   bind :: [Parser a, (a -> Parser b)] -> Parser b

  Kind of 'Apply parser `p` and then apply the parser `p2`
  you get by applying `f` to the result of `p`'"
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
        (pu/->failure "or-c had no more parsers"
                      {:parsers-failures failures})
        (match (pu/parse p input)
               [r rsin] [r rsin]
               (f :guard pu/failure?) (recur ps (conj failures f) res))))))

(defn ?
  "Optionally parses one `p`, returning a seq containing it if found.
  If `p` doesn't match, returns an empty seq"
  [p]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] [[r] rsin]
           (f :guard pu/failure?) [[] input])))

(defn *
  "Parse `p` 0 or more times. Similar to `*` in regular expressions."
  ;; NOTE cleverer impl to feel good about myself?
  ;;      worth leaving for perf? FIXME:
  [p]
  (fn [input]
    (loop [results    []
           rest-input input]
      (match (pu/parse p rest-input)
             [r rsin] (recur (conj results r) rsin)
             (_ :guard pu/failure?) [results rest-input]))))

(defn vanity*
  "Alternative, way cooler implementation of *"
  [p]
  (bind (? p)
        (fn [r] (fmap (partial concat r) (* p)))))

(defn +
  [p]
  (bind p
        (fn [r]
          (fmap #(conj % r)
                (* p)))))

(defn merge
  "Applies `parsers` in order and then merges their results into one big fat map."
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

(defn skip*
  "Skips 0 or more p."
  [p]
  (fmap (fn [_] nil) (* p)))

(defn skip+
  "Skips 1 or more p."
  [p]
  (fmap (fn [_] nil) (+ p)))


