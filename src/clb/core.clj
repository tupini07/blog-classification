 (ns clb.core
  (:gen-class)
  (:require [clojure.string :as st]
            [clb.rss-parse :as rsp]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [clb.feed-process :as fp]))

(defn process-feeds-and-create-dataset []
  "Exactly what the name suggests"
  (let [feeds           (fp/get-feedlist)
        [all-wc ttl-wc] (fp/process-feeds feeds)
        wordlist        (fp/get-wordlist ttl-wc feeds)]
    (fp/create-dataset all-wc wordlist)))


(defn -main
  "Cows"
  [& args]

  )
