(ns parseq.utils
  "Helpers for dealing with parsers and their results")

(defn parse
  "Apply parser `p` to `input` (i.e. helper to run the parse)."
  [p input]
  (p input))

(defn success?
  "Checks that the result of a parse has succeeded."
  [parse-result]
  (and (vector? parse-result)
       (= 2 (count parse-result))))

(def ^{:doc "Checks if the result of a parse has failed."}
  failure? (comp not success?))

(defn all-input-parsed?
  "Checks that the result of a parse has successfully parsed
  all the input."
  [parse-result]
  (and (success? parse-result)
       ;; god i miss types..
       (vector? parse-result)
       (= 2 (count parse-result))
       (nil? (second parse-result))))
(def ^{:doc "Alias to `all-input-parsed?`"}
  complete-success? all-input-parsed?)

(defn value
  "Extracts the value from a successful parse result."
  [parse-result]
  (when (success? parse-result)
    (first parse-result)))

(defn ->failure
  "Constructs a parser Failure with optional data."
  ([msg] (->failure msg {}))
  ([msg data] (merge data
                     {:failure-msg msg})))
