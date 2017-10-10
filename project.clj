(defproject vise890/parseq "0.2.1"
  :description "Parser combinators for clojure data"

  :url "http://github.com/vise890/parseq"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]]

  :profiles {:ci {:deploy-repositories
                  [["clojars" {:url           "https://clojars.org/repo"
                               :username      :env
                               :password      :env
                               :sign-releases false}]]}})
