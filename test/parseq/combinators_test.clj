(ns parseq.combinators-test
  (:require [clojure.test :refer :all]
            [parseq.combinators :as sut-c]
            [parseq.parsers :as p]
            [parseq.utils :as pu]))

(deftest peek-c

  (testing "consumes no input"
    (is (= [1 [1 [2 3]]]
           (pu/parse (sut-c/peek p/one) [1 [2 3]]))))

  (testing "fails when there's no more input left"
    (is (pu/failure?
         (pu/parse (sut-c/peek p/one) [])))))

(deftest many-c
  (is (= [[:a :b :c :d] nil]
         (pu/parse (sut-c/many p/one) [:a :b :c :d])))

  (testing "stops parsing correctly"
    (is (= [[1 1 1] [:a :a :a]]
           (pu/parse (sut-c/many (p/->one= 1)) [1 1 1 :a :a :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut-c/many p/one) [])))
    (is (= [[] nil]
           (pu/parse (sut-c/many p/one) nil)))))

(deftest or-c
  (let [res (pu/parse (sut-c/or p/fail
                                p/fail)
                      [[:hi]])
        pfs (:parsers-failures res)]
    (is (pu/failure? res))
    (is (every? pu/failure? pfs))
    (is (= 2 (count pfs))))

  (is (= [[:hi :bar] nil]
         (pu/parse (sut-c/or p/fail
                             p/fail
                             p/fail
                             p/one)
                   [[:hi :bar]]))))

(deftest bind
  ;;; (a.k.a >>=)
  (is (= [2 [3]]
         (pu/parse (sut-c/bind p/one
                               (fn [_2] p/one))
                   [1 2 3])))

  (is (pu/failure? (pu/parse (sut-c/bind p/fail
                                         (fn [_] p/one))
                             [[]]))))

(deftest fmap
  (is (= [{:one 1} [:fin]]
         (pu/parse (sut-c/fmap (fn [one] {:one one})
                               (p/->one= 1))
                   [1 :fin]))))

(deftest merge-c
  (let [p   (sut-c/merge [(sut-c/fmap (fn [one] {:one one})
                                      (p/->one= 1))
                          (sut-c/fmap (fn [two] {:two two})
                                      (p/->one= 2))
                          (sut-c/fmap (fn [three] {:three three})
                                      (p/->one= 3))])
        act (pu/parse p [1 2 3 :nope])]
    (is (= [{:one   1
             :two   2
             :three 3} [:nope]]
           act)))

  (testing "more parsers than groups should fail"
    (is (pu/failure? (pu/parse (sut-c/merge [p/one p/one])
                               [[1 2]]))))

  (testing "parses nil input if no parsers specified"
    (is (= [{} nil]
           (pu/parse (sut-c/merge []) nil))))

  (testing "no parsers yields an empty map and consumes no input"
    (is (= [{} [[1 2]]]
           (pu/parse (sut-c/merge []) [[1 2]])))))
