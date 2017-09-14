(ns parseq.core-test
  (:require [clojure.core.match :refer [match]]
            [clojure.test :refer :all]
            [parseq.combinators :as sut-c]
            [parseq.parsers :as sut-p]
            [parseq.utils :as pu]))

(deftest one-p

  (is (= [1 [2]]
         (pu/parse sut-p/one-p [1 2])))

  (is (= [[1 2] nil]
         (pu/parse sut-p/one-p [[1 2]])))

  (testing "failing sut-p/one-p"
    (is (pu/failure? (pu/parse sut-p/one-p nil)))))

(deftest one-p

  (is (= [1 [2]]
         (pu/parse (sut-p/->one=-p 1) [1 2])))

  (testing "failing sut-p/one=-p"
    (is (pu/failure? (pu/parse (sut-p/->one=-p 1) [:a :b])))))

(deftest peek-c

  (testing "consumes no input"
    (is (= [1 [1 [2 3]]]
           (pu/parse (sut-c/peek-c sut-p/one-p) [1 [2 3]]))))

  (testing "fails when there's no more input left"
    (is (pu/failure?
         (pu/parse (sut-c/peek-c sut-p/one-p) [])))))

(deftest many-c
  (is (= [[:a :b :c :d] nil]
         (pu/parse (sut-c/many-c sut-p/one-p) [:a :b :c :d])))

  (testing "stops parsing correctly"
    (is (= [[1 1 1] [:a :a :a]]
           (pu/parse (sut-c/many-c (sut-p/->one=-p 1)) [1 1 1 :a :a :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut-c/many-c sut-p/one-p) [])))
    (is (= [[] nil]
           (pu/parse (sut-c/many-c sut-p/one-p) nil)))))

(deftest or-c
  (let [res (pu/parse (sut-c/or-c sut-p/fail-p
                                  sut-p/fail-p)
                      [[:hi]])
        pfs (:parsers-failures res)]
    (is (pu/failure? res))
    (is (every? pu/failure? pfs))
    (is (= 2 (count pfs))))

  (is (= [[:hi :bar] nil]
         (pu/parse (sut-c/or-c sut-p/fail-p
                               sut-p/fail-p
                               sut-p/fail-p
                               sut-p/one-p)
                   [[:hi :bar]]))))

(deftest bind
  ;;; (a.k.a >>=)
  (is (= [2 [3]]
         (pu/parse (sut-c/bind sut-p/one-p
                               (fn [_2] sut-p/one-p))
                   [1 2 3])))

  (is (pu/failure? (pu/parse (sut-c/bind sut-p/fail-p
                                         (fn [_] sut-p/one-p))
                             [[]]))))

(deftest fmap
  (is (= [{:one 1} [:fin]]
         (pu/parse (sut-c/fmap (fn [one] {:one one})
                               (sut-p/->one=-p 1))
                   [1 :fin]))))


(deftest merge-c
  (let [p   (sut-c/merge-c [(sut-c/fmap (fn [one] {:one one})
                                        (sut-p/->one=-p 1))
                            (sut-c/fmap (fn [two] {:two two})
                                        (sut-p/->one=-p 2))
                            (sut-c/fmap (fn [three] {:three three})
                                        (sut-p/->one=-p 3))])
        act (pu/parse p [1 2 3 :nope])]
    (is (= [{:one   1
             :two   2
             :three 3} [:nope]]
           act)))

  (testing "more parsers than groups should fail"
    (is (pu/failure? (pu/parse (sut-c/merge-c [sut-p/one-p sut-p/one-p])
                               [[1 2]]))))

  (testing "parses nil input if no parsers specified"
    (is (= [{} nil]
           (pu/parse (sut-c/merge-c []) nil))))

  (testing "no parsers yields an empty map and consumes no input"
    (is (= [{} [[1 2]]]
           (pu/parse (sut-c/merge-c []) [[1 2]])))))

