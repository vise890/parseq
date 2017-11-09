(ns parseq.combinators-test
  (:require [clojure.test :refer :all]
            [parseq.combinators :as sut]
            [parseq.parsers :as p]
            [parseq.utils :as pu]))

(deftest sut-or
  (testing "lets you specify alternatives"
    (is (= [:hi [:hola]]
           (pu/parse (sut/or p/fail p/one) [:hi :hola]))))

  (testing "works at the end of input"
    (is (= [:hi nil]
           (pu/parse (sut/or p/fail p/one) [:hi]))))

  (testing "fails when no parser matches"
    (is (pu/failure? (pu/parse (sut/or p/fail)
                               [:beep])))

    (testing "keeps track of failures"
      (let [res (pu/parse (sut/or p/fail
                                  p/fail)
                          [:hi])
            pfs (:parsers-failures res)]
        (is (pu/failure? res))
        (is (every? pu/failure? pfs))
        (is (= 2 (count pfs)))))))

(deftest sut-and
  (testing "specifies parsers to be applied in succession"
    (is (= [[1 2] [:a :a]]
           (pu/parse (sut/and (p/one= 1) (p/one= 2))
                     [1 2 :a :a]))))

  (testing "fails when not all parsers match"
    (is (pu/failure? (pu/parse (sut/and (p/one= 1)
                                        (p/one= 2))
                               [1 3])))))

(deftest one?
  (testing "applies the parser"
    (is (= [[:a] [:b :c]]
           (pu/parse (sut/one? p/one)
                     [:a :b :c]))))

  (testing "does not fail if the parser fails"
    (is (= [[] [:a :b :c]]
           ;; NOTE `one?` is a.k.a. `optional`
           (pu/parse (sut/optional p/fail)
                     [:a :b :c])))))

(deftest many*
  (testing "takes 0 or more repetitions"
    (is (= [[:a :b :c] nil]
           (pu/parse (sut/many* p/one) [:a :b :c])))
    (is (= [[] [:a :b :c]]
           (pu/parse (sut/many* p/fail) [:a :b :c]))))

  (testing "can be combined with sut/or"
    (is (= [[:a :b :a] [:c :c]]
           (pu/parse (sut/many* (sut/or (p/one= :a)
                                        (p/one= :b)))
                     [:a :b :a :c :c]))))

  (testing "stops parsing correctly"
    (is (= [[1 1 1] [:a :a :a]]
           (pu/parse (sut/many* (p/one= 1)) [1 1 1 :a :a :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut/many* p/one) [])))
    (is (= [[] nil]
           (pu/parse (sut/many* p/one) nil)))))

(deftest many+
  (testing "takes multiple elements"
    (is (= [[:a :a] [:b :c]]
           (pu/parse (sut/many+ (p/one= :a))
                     [:a :a :b :c]))))

  (testing "requires at least one match"
    (is (pu/failure? (pu/parse (sut/many+ (p/one= :a))
                               [:f :o :o]))))

  (testing "maintains order"
    (is (= [[1 2] [:a]]
           (pu/parse (sut/many+ (p/one-satisfying integer?))
                     [1 2 :a])))
    (is (= [["a" "b"] [:c]]
           (pu/parse (sut/many+ (p/one-satisfying string?))
                     ["a" "b" :c])))))

(deftest skip*
  (testing "returns nil"
    (is (= [nil [:b]]
           (pu/parse (sut/skip* (p/one= :a)) [:a :a :b]))))

  (testing "parses 0 repetitions"
    (is (= [nil [:a :b]]
           (pu/parse (sut/skip* (p/one= :b)) [:a :b])))))

(deftest skip+
  (testing "returns nil"
    (is (= [nil [:b]]
           (pu/parse (sut/skip+ (p/one= :a)) [:a :a :b]))))

  (testing "requires at least one match"
    (is (pu/failure? (pu/parse (sut/skip+ (p/one= :b))
                               [:a :b])))))

(deftest sut-merge
  (let [p   (sut/merge [(sut/fmap (fn [one] {:one one})
                                  (p/one= 1))
                        (sut/fmap (fn [two] {:two two})
                                  (p/one= 2))
                        (sut/fmap (fn [three] {:three three})
                                  (p/one= 3))])
        act (pu/parse p [1 2 3 :nope])]
    (is (= [{:one   1
             :two   2
             :three 3} [:nope]]
           act)))

  (testing "more parsers than groups should fail"
    (is (pu/failure? (pu/parse (sut/merge [p/one p/one])
                               [[1 2]]))))

  (testing "parses nil input if no parsers specified"
    (is (= [{} nil]
           (pu/parse (sut/merge []) nil))))

  (testing "no parsers yields an empty map and consumes no input"
    (is (= [{} [[1 2]]]
           (pu/parse (sut/merge []) [[1 2]])))))

(deftest sut-peek
  (testing "consumes no input"
    (is (= [1 [1 [2 3]]]
           (pu/parse (sut/peek p/one) [1 [2 3]]))))

  (testing "fails when there's no more input left"
    (is (pu/failure?
         (pu/parse (sut/peek p/one) [])))))

(deftest bind
  (is (= [2 [3]]
         (pu/parse (sut/bind p/one
                             (fn [_2] p/one))
                   [1 2 3])))

  (is (pu/failure? (pu/parse (sut/bind p/fail
                                       (fn [_] p/one))
                             [[]]))))

(deftest fmap
  (is (= ["1" [:fin]]
         (pu/parse (sut/fmap str p/one) [1 :fin]))))

(deftest unordered
  (let [p (sut/many* (sut/or (p/one= :a)
                             (p/one= :b)))]
    (is (= [[:a :b :a] [:c]]
           (pu/parse p [:a :b :a :c]))))

  (testing "A more complex example"
    (let [p (sut/fmap #(apply (partial merge-with concat) %)
                      (sut/many+ (sut/or (sut/fmap (fn [one] {:one one})
                                                   (sut/many+ (p/one= 1)))
                                         (sut/fmap (fn [two] {:two two})
                                                   (sut/many+ (p/one= 2)))
                                         (sut/fmap (fn [three] {:three three})
                                                   (sut/many+ (p/one= 3))))))]
      (is (= [{:one   [1 1 1]
               :two   [2 2]
               :three [3 3]} [:nope]]
             (pu/parse p [1 1 3 2 2 1 3 :nope]))))))
