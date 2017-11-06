(ns parseq.combinators
  "Combinators for `parseq.parsers` and similar"
  (:refer-clojure :exclude [or and merge peek])
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

  bind :: [Parser a, (a -> Parser b)] -> Parser b"
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
    (loop [parsers  parsers
           failures []]
      (if-let [[p & ps] (seq parsers)]
        (match (pu/parse p input)
               [r rsin] [r rsin]
               (f :guard pu/failure?) (recur ps (conj failures f)))
        (pu/->failure "or-c had no more parsers"
                      {:parsers-failures failures})))))

(defn and
  [& parsers]
  (fn [input]
    (loop [parsers parsers
           res []
           input input]
      (if-let [[p & ps] (seq parsers)]
        (match (pu/parse p input)
               [r rsin] (recur ps (conj res r) rsin)
               (f :guard pu/failure?) f)
        [res input]))))

(defn one?
  "Optionally parses one `p`, returning a seq containing it if found. If `p`
  doesn't match, returns an empty seq"
  [p]
  (fn [input]
    (match (pu/parse p input)
           [r rsin] [[r] rsin]
           (f :guard pu/failure?) [[] input])))
(def ^{:doc "Alias to `one?`"} optional one?)

(defn many*
  "Parse `p` 0 or more times. Similar to `*` in regular expressions."
  [p]
  (fn [input]
    (loop [results    []
           rest-input input]
      (match (pu/parse p rest-input)
             [r rsin] (recur (conj results r) rsin)
             (_ :guard pu/failure?) [results rest-input]))))

(defn many+
  "Parse `p` 1 or more times. Similar to `+` in regular expressions."
  [p]
  (bind p
        (fn [r]
          (fmap #(cons r %) ;; FIXME this is not ideal, rewrite as a loop
                (many* p)))))

(defn merge
  "Applies `parsers` in order and then merges their results into one big fat
  map."
  [parsers]
  (fn [input]
    (loop [parsers    parsers
           rest-input input
           results    {}]
      (if-let [[p & ps] (seq parsers)]
        (match (pu/parse p rest-input)
               [r rsin] (recur ps rsin (conj results r))
               (f :guard pu/failure?) f)
        [results rest-input]))))

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
