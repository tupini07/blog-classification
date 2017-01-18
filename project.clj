(defproject clb "0.1.0-SNAPSHOT"
  :description "Given a list of blogs, read their RSS feed and
                classify them using clustering based on their words"
  :url "https://github.com/tupini07/classify-blogs"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojars.freemarmoset/feedparser-clj "0.6.1"]
                 [org.clojure/math.combinatorics "0.1.4"]]
  :main ^:skip-aot clb.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
