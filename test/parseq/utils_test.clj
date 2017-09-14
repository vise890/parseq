(ns parseq.utils-test
  (:require [clojure.test :refer :all]
            [parseq.parsers :as p]
            [parseq.utils :as sut]))

(deftest parsing
  (is (= [1 nil] (sut/parse p/one [1]))))

(deftest asserting-all-input-parsed?
  (is (sut/all-input-parsed? (sut/parse p/one [1])))
  (is (sut/complete-success? (sut/parse p/one [1]))))

(deftest success-and-failure
  (let [res (sut/parse p/one [:fml 2])]
    (is (= [:fml [2]] res))
    (is (sut/success? res))
    (is (not (sut/failure? res)))
    (is (not (sut/complete-success? res)))))

(deftest getting-parse-results
  (let [res (sut/parse p/one [:a :b])]
    (is (not (sut/failure? res)))
    (is (= :a (sut/value res))))

  (testing "returns nil when parsing failed"
    ;; TODO how do we differentiate this from `nil` as a result? huh?
    (let [failed (sut/parse p/one [])]
      (is (sut/failure? failed))
      (is (nil? (sut/value failed))))))
