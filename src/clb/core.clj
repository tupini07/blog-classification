(ns clb.core
  (:gen-class)
  (:require [clojure.string :as st]
            [clb.feed-parse :as fp]))

(defn get-feedlist []
  "returns an array of feed URLs. This are taken from a 
   file called 'feeds.txt' in the resources folder."
  (-> (clojure.java.io/resource "feeds.txt")
      .getPath
      (st/replace #"%20" " ") ; adaptation for paths with spaces
      slurp
      (st/replace #"\r" "")
      (st/split #"\n")))

(defn process-feeds [feeds]
  (let [all-wc (->> feeds ; Construct map consisting of all titles and their wc 
                    (map fp/word-count)
                    (apply concat)
                    merge)
        ttl-wc (->> (map :wc all-wc) ; get all wc maps 
                    (apply merge-with +))] ; merge all wc maps with +
    [all-wc ttl-wc]))


(def feeds [
            "https://gigaom.com/feed/"
            "http://feeds.feedburner.com/37signals/beMH"
            "http://feeds.feedburner.com/blogspot/bRuz"
            ])

(def pff (process-feeds feeds))
(def a (first pff))
(def b (second pff))


(defn -main
  "Cows"
  [& args]

  )
