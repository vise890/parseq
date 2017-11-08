(defproject vise890/parseq "0.2.9"
  :description "Parser Combinators for Clojure data"

  :url "http://gitlab.com/vise890/parseq"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]]

  :plugins [[lein-codox "0.10.3"]]

  :profiles {:dev {:dependencies [[criterium "0.4.4"]]}
             :ci  {:deploy-repositories
                   [["clojars" {:url           "https://clojars.org/repo"
                                :username      :env
                                :password      :env
                                :sign-releases false}]]}})
