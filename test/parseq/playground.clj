(ns parseq.playground)

(require '[parseq.utils :as pu])
(require '[parseq.combinators :as pc])
(require '[parseq.parsers :as pp])

(pu/parse pp/one [1 2])
;; => [1 [2]]

(pu/parse pp/one [[1 2]])
;; => [[1 2] nil]

(pu/parse (pp/one-satisfying odd?) [3 2])
;; => [3 [2]]

(pu/parse (pp/one-not-satisfying even?) [3 2])
;; => [3 [2]]


(pu/parse (pp/one= 1) [1 2])
;; => [1 [2]]

(pu/parse (pp/one-not= :a) [1 2])
;; => [1 [2]]

(pu/parse (pc/fmap (fn [one] {:one one})
                   (pp/one= 1))
          [1 :fin])
;; => [{:one 1} [:fin]]

(pu/parse (pc/many* (pc/or (pp/one= 1)
                           (pp/one= 2)))
          [1 1 2 :a :a])
;; => [[1 1 2] [:a :a]]

(pu/parse (pc/and (pp/one= 1) (pp/one= 2))
          [1 2 :a :a])
;; => [[1 2] [:a :a]]

(pu/parse (pc/one? pp/one)
          [:a :b :c])
;; => [[:a] [:b :c]]

(pu/parse (pc/many* pp/one)
          [:a :b :c :d])
;; => [[:a :b :c :d] nil]

(pu/parse (pc/many+ (pp/one= :a))
          [:a :a :b :c])
;; => [(:a :a) [:b :c]]

(pu/parse (pc/peek pp/one) [1 [2 3]])
;; => [1 [1 [2 3]]]

(pu/parse (pc/skip* (pp/one= :a))
          [:a :a :b])
;; => [nil [:b]]

(pu/parse (pc/skip+ (pp/one= :a))
          [:a :a :b])
;; => [nil [:b]]

;;,------
;;| Utils
;;`------

(pu/success? (pu/parse (pp/one= :ciao) [:ciao]))
;; => true
(pu/failure? (pu/parse (pp/one= :ciao) [:hola]))
;; => true
(pu/parse pp/fail [:beep])
;; => {:failure-msg "`fail` failed, it always does that..."}
