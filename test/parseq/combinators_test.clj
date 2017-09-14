(ns parseq.combinators-test
  (:require [clojure.test :refer :all]
            [parseq.combinators :as sut]
            [parseq.parsers :as p]
            [parseq.utils :as pu]))

(deftest bind

  (is (= [2 [3]]
         (pu/parse (sut/bind p/one
                             (fn [_2] p/one))
                   [1 2 3])))

  (is (pu/failure? (pu/parse (sut/bind p/fail
                                       (fn [_] p/one))
                             [[]]))))

(deftest fmap
  (is (= [{:one 1} [:fin]]
         (pu/parse (sut/fmap (fn [one] {:one one})
                             (p/one= 1))
                   [1 :fin]))))

(deftest or-c

  (is (= [[1 1 2] [:a :a]]
         (pu/parse (sut/many* (sut/or (p/one= 1)
                                      (p/one= 2)))
                   [1 1 2 :a :a])))

  (is (= [:hi nil]
         (pu/parse (sut/or p/fail
                           p/fail
                           p/fail
                           p/one)
                   [:hi])))

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

(deftest ?-c

  (is (= [[:a] [:b :c]]
         (pu/parse (sut/one? p/one)
                   [:a :b :c])))

  (is (= [[] [:a :b :c]]
         (pu/parse (sut/one? (p/one= :boop))
                   [:a :b :c]))))

(deftest *-c
  (is (= [[:a :b :c :d] nil]
         (pu/parse (sut/many* p/one) [:a :b :c :d])))

  (testing "stops parsing correctly"
    (is (= [[1 1 1] [:a :a :a]]
           (pu/parse (sut/many* (p/one= 1)) [1 1 1 :a :a :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut/many* p/one) [])))
    (is (= [[] nil]
           (pu/parse (sut/many* p/one) nil)))))

(deftest vanity*
  (is (= [[:a :b :c :d] nil]
         (pu/parse (sut/vanity-many* p/one) [:a :b :c :d])))

  (testing "stops parsing correctly"
    (is (= [[1 1 1] [:a :a :a]]
           (pu/parse (sut/vanity-many* (p/one= 1)) [1 1 1 :a :a :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut/vanity-many* p/one) [])))
    (is (= [[] nil]
           (pu/parse (sut/vanity-many* p/one) nil)))))

(deftest +-c
  (is (= [[:a :a] [:b :c]]
         (pu/parse (sut/many+ (p/one= :a))
                   [:a :a :b :c])))

  (is (pu/failure? (pu/parse (sut/many+ (p/one= :a))
                             [:f :o :o])))

  (testing "maintains order"
    (is (= [[1 2 3] [:a]]
           (pu/parse (sut/many* (sut/or (p/one= 2)
                                        (p/one= 1)
                                        (p/one= 3)))
                     [1 2 3 :a]))))

  (testing "returns [] when input is nil"
    (is (= [[] []]
           (pu/parse (sut/many* p/one) [])))
    (is (= [[] nil]
           (pu/parse (sut/many* p/one) nil)))))

(deftest merge-c
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

(deftest peek-c

  (testing "consumes no input"
    (is (= [1 [1 [2 3]]]
           (pu/parse (sut/peek p/one) [1 [2 3]]))))

  (testing "fails when there's no more input left"
    (is (pu/failure?
         (pu/parse (sut/peek p/one) [])))))

(deftest skip*-c
  (is (= [nil [:b]]
         (pu/parse (sut/skip* (p/one= :a)) [:a :a :b])))

  (testing "parses 0 repetitions"
    (is (= [nil [:a :b]]
           (pu/parse (sut/skip* (p/one= :b)) [:a :b])))))

(deftest skip+-c
  (is (= [nil [:b]]
         (pu/parse (sut/skip+ (p/one= :a)) [:a :a :b])))

  (testing "fails when parser doesn't match first repetition"
    (is (pu/failure? (pu/parse (sut/skip+ (p/one= :b))
                               [:a :b])))))
