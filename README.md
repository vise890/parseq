# parseq

A small library of [parser
combinators](https://en.wikipedia.org/wiki/Parser_combinator) that operate on
Clojure sequential collections rather than strings.

## [API Docs](https://vise890.gitlab.io/parseq/)

## Usage

[![Clojars
Project](https://img.shields.io/clojars/v/vise890/parseq.svg)](https://clojars.org/vise890/parseq)

```clojure
;;; project.clj
[vise890/parseq "0.2.7"]
```

```clojure
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
```


## Intro to Parser Combinators

A Parser is something that takes a `seq` as input and returns a parsing result:

```
Parser :: [a] -> Result
```

A `Result` can be either a `Success` or a `Failure`:

```
Result :: Success | Failure
```

A `Success` looks like this:

```clojure
[something, [rest-input]]
```

Where `something` is what the was parsed, and the second element in the tuple
is the remaining input.

A `Failure` is a map like this:

```clojure
{:failure msg :any-other-key true}.
```

You can add more info to a `Failure`, like the input that was left, the parser
that was being used at the time, the reason for failure and any other data that
you may have that may be useful to debug.

So to recap:

```
Parser     :: Input -> Result
Result     :: [a, InputLeft] || Failure (+ info maybe)
Combinator :: Parser a -> Parser b
```

### Cool Links about Parser Combinators

* [**good, 2-minute intro**](http://theorangeduck.com/page/you-could-have-invented-parser-combinators)
* [A bit more in depth / rigorous](http://sigusr2.net/parser-combinators-made-simple.html)
* [In Clojure, but more advanced](https://gist.github.com/kachayev/b5887f66e2985a21a466)
* Haskell's Parsec and its derivatives (Attoparsec, MegaParsec), where this all comes from

## License

Copyright © 2017 Martino Visintin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

