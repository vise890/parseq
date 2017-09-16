# parseq

A small library of [parser
combinators](https://en.wikipedia.org/wiki/Parser_combinator) that operate on
clojure `seq`s rather than strings.

**NOTE**: it doesn't actually (yet) work on `seq`s but rather on sequential
collections

## Intro

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

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vise890/parseq.svg)](https://clojars.org/vise890/parseq)

```clojure
;;; project.clj
[vise890/parseq "0.1.0-SNAPSHOT"]
```

```clojure
(ns parseq.core-test
  (:require [parseq.combinators :as c]
            [parseq.parsers :as p]
            [parseq.utils :as pu]))

(pu/parse p/one [1 2])
;; => [1 [2]]

(pu/parse p/one [[1 2]])
;; => [[1 2] nil]

(pu/parse (c/peek p/one) [1 [2 3]])
;; => [1 [1 [2 3]]]

(pu/parse (c/many* (p/one= 1)) [1 1 1 :a :a :a])
;; => [[1 1 1] [:a :a :a]]

(pu/parse (c/many* (c/or (p/one= 1)
                         (p/one= 2)))
          [1 1 1 2 2 2 :a :a :a])
;; => [[1 1 1 2 2 2] [:a :a :a]]

;; there's a bit more, but you'll have to look at the source for now

;; TODO add more examples / docs
```

## Cool Links about Parser Combinators

* [**good, 2-minute intro**](http://theorangeduck.com/page/you-could-have-invented-parser-combinators)
* [A bit more in depth / rigorous](http://sigusr2.net/parser-combinators-made-simple.html)
* [In Clojure, but more advanced](https://gist.github.com/kachayev/b5887f66e2985a21a466)
* Haskell's Parsec and its derivatives (Attoparsec, MegaParsec), where this all comes from

## License

Copyright © 2017 Martino Visintin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

