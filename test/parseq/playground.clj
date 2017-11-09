(ns parseq.playground
  (:require [parseq.utils :as pu]))

(require '[parseq.utils :as pu])
(require '[parseq.combinators :as pc])
(require '[parseq.parsers :as pp])

;;,--------
;;| Parsers
;;`--------
;; pp/one takes one element unconditionally:
(pu/parse pp/one [:beep :boop])
;; => [:beep [:boop]]

;; take elements that satisfy predicates (or not):
(pu/parse (pp/one-satisfying odd?) [3 2])
;; => [3 [2]]
(pu/parse (pp/one-not-satisfying even?) [3 2])
;; => [3 [2]]
(pu/parse (pp/one= :fu) [:fu 2])
;; => [:fu [2]]
(pu/parse (pp/one-not= :a) [1 2])
;; => [1 [2]]

;;,------------
;;| Combinators
;;`------------
;; pc/or indicates alternative parsers
(pu/parse (pc/or (pp/one= :a)
                 (pp/one= :b))
          [:b])
;; => [:b []]

;; pc/and combines parsers in a sequence
(pu/parse (pc/and (pp/one= 1) (pp/one= 2))
          [1 2 :a :a])
;; => [[1 2] [:a :a]]


;; pc/one? tries a parser...
(pu/parse (pc/one? (pp/one= :a)) [:a :b])
;; => [[:a] [:b]]
;; .. but doesn't fail if it can't find a match
(pu/parse (pc/one? (pp/one= :a)) [:x :y :z])
;; => [[] [:x :y :z]]

;; pc/many* takes zero or more repetitions of the same parser
(pu/parse (pc/many* (pp/one= :a)) [:a :a :a :c :c])
;; => [[:a :a :a] [:c :c]]

;; pc/many+ requires at least one parse:
(pu/parse (pc/many+ (pp/one= :a)) [:a :a :b :c])
;; => [(:a :a) [:b :c]]
(pu/parse (pc/many+ (pp/one= :a)) [:x :y :z])
;; => {:input [:x :y :z]
;;     :failure-msg "one-satisfying fail"}

;; you can also skip elements if you don't care:
(pu/parse (pc/skip* (pp/one= :a)) [:a :a :b]) ;; 0 or more
;; => [nil [:b]]
(pu/parse (pc/skip+ (pp/one= :a)) [:a :a :b]) ;; 1 or more
;; => [nil [:b]]

;; pc/peek lets you look without consuming input:
(pu/parse (pc/peek pp/one) [:a :b :c])
;; => [:a [:a :b :c]]

;; pc/fmap transforms the result of a parse
(pu/parse (pc/fmap str (pp/one= 1)) [1 :fin])
;; => ["1" [:fin]]

;;,------
;;| Utils
;;`------

(pu/success? (pu/parse (pp/one= :ciao) [:ciao]))
;; => true
(pu/failure? (pu/parse (pp/one= :ciao) [:hola]))
;; => true
(pu/all-input-parsed? (pu/parse pp/one [:hey]))
;; => true
(pu/all-input-parsed? (pu/parse pp/one [:bundì :mandi]))
;; => false

(pu/parse pp/fail [:beep])
;; => {:failure-msg "`fail` failed, it always does that..."}
