(ns parseq.utils
  "Helpers for dealing with parsers and their results")

(defn parse
  "A helper function to invoke parser p on input"
  [p input]
  (p input))

(defn success?
  "Checks that the result of a parse has succeeded"
  [parse-result]
  (and (vector? parse-result)
       (= 2 (count parse-result))))

(def failure? (comp not success?))

(defn all-input-parsed?
  "Checks that the result of a parse has successfully parsed
  all the input"
  [parse-result]
  (and (success? parse-result)
       ;; god i miss types..
       (vector? parse-result)
       (= 2 (count parse-result))
       (nil? (second parse-result))))
(def complete-success? all-input-parsed?)

(defn value
  "Extracts the value from a successful parse result"
  [parse-result]
  (when (success? parse-result)
    (first parse-result)))

(defn ->failure
  "Constructs a parser failure with optional data"
  ([msg] (->failure msg {}))
  ([msg data] (merge data
                     {:failure-msg msg})))

