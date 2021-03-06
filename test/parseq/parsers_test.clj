(ns parseq.parsers-test
  (:require [clojure.test :refer :all]
            [parseq.parsers :as sut]
            [parseq.utils :as pu]))

(deftest fail
  (testing "always fails"
    (is (pu/failure? (pu/parse sut/fail [1 2])))))

(deftest one
  (testing "takes one element unconditionally"
    (is (= [:beep [:boop]]
           (pu/parse sut/one [:beep :boop]))))

  (testing "fails when no more input"
    (is (pu/failure? (pu/parse sut/one nil)))))

(deftest one-satisfying
  (testing "takes elements that satisfy a predicate"
    (is (= [3 [2]]
           (pu/parse (sut/one-satisfying odd?) [3 2]))))

  (testing "fails when input doesn't match"
    (is (pu/failure? (pu/parse (sut/one-satisfying odd?) [2])))))

(deftest one-not-satisfying
  (testing "takes elements that do not satisfy a predicate"
    (is (= [3 [2]]
           (pu/parse (sut/one-not-satisfying even?) [3 2]))))

  (testing "fails when input doesn't match"
    (is (pu/failure? (pu/parse (sut/one-not-satisfying even?) [2])))))

(deftest one=
  (is (= [1 [2]]
         (pu/parse (sut/one= 1) [1 2])))

  (testing "fails when input doesn't match"
    (is (pu/failure? (pu/parse (sut/one= 1) [:a :b])))))

(deftest one-not=
  (is (= [1 [2]]
         (pu/parse (sut/one-not= :a) [1 2])))

  (testing "fails when input matches"
    (is (pu/failure? (pu/parse (sut/one-not= :foo) [:foo :b])))))
