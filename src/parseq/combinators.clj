(ns parseq.combinators
  "Combinators for `parseq.parsers` and similar"
  (:refer-clojure :exclude [or merge peek])
  (:require [clojure.core.match :refer [match]]
            [parseq.utils :as pu]))

(defn return
  "A parser that does nothing and always succeeds. It returns the input
  unchanged and the supplied `v` as a result."
  [v]
  (fn [input] [v input]))

(defn bind
  "This is a monadic bind. A.k.a. >>=

  Its type is:

  bind :: [Parser a, (a -> Parser b)] -> Parser b

  It roughly means:
  1. Apply parser `p`, if that fails, the whole thing fails
  2. If it didn't fail, you got a result, say `r`
  3. Apply `f` to `r` to get another parser, say (`p2`) (look at the type above)
  4. Apply `p2` and return **its** result"
  [p f]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] (pu/parse (f r)  rsin)
           (r :guard pu/failure?) r)))

(defn fmap
  "Applies function `f` to the result of `p`

  Its type is:

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

(defn one?
  "Optionally parses one `p`, returning a seq containing it if found. If `p`
  doesn't match, returns an empty seq"
  [p]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] [[r] rsin]
           (f :guard pu/failure?) [[] input])))
(def optional one?)

(defn many*
  "Parse `p` 0 or more times. Similar to `*` in regular expressions."
  [p]
  (fn [input]
    (loop [results    []
           rest-input input]
      (match (pu/parse p rest-input)
             [r rsin] (recur (conj results r) rsin)
             (_ :guard pu/failure?) [results rest-input]))))

(defn vanity-many*
  "Alternative, way cooler implementation of many*"
  [p]
  (bind (one? p)
        (fn [r]
          (fmap (partial concat r)
                (many* p)))))

(defn many+
  [p]
  (bind p
        (fn [r]
          (fmap #(conj % r)
                (many* p)))))

(defn merge
  "Applies `parsers` in order and then merges their results into one big fat
  map."
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
  (fmap (fn [_] nil) (many* p)))

(defn skip+
  "Skips 1 or more p."
  [p]
  (fmap (fn [_] nil) (many+ p)))

