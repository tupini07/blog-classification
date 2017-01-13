(ns clb.core
  (:gen-class)
  (:require [clojure.string :as st]
            [clb.feed-parse :as fp]))

(defn rsrc-path [fname]
  (->  (clojure.java.io/resource fname)
       .getPath
       (st/replace #"%20" " "))) ; adaptation for paths with spaces

(defn get-feedlist []
  "returns an array of feed URLs. This are taken from a 
   file called 'feeds.txt' in the resources folder."
  (-> (rsrc-path "feeds.txt")
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

(defn get-words [total-wc feedlist]
  "given the lenght of our wordlist and a map of all the worcounts
   we return an array of words whose percentage of ocurrence is between
   a range (not too common and not to rare) "
  (def upper-limit 0.5)
  (def lower-limit 0.1)
  
  (def feedlen (count feedlist))
  (->> total-wc
       (filter (fn [[w fr]] 
                 (let [frac (/ (float fr) feedlen)]
                   (< 0.1 frac 0.5))))
       (map first)))


(def feeds [ ; test feeds so we don't have to load aaaalll the feeds when testing
            "https://gigaom.com/feed/"
            "http://feeds.feedburner.com/37signals/beMH"
            "http://feeds.feedburner.com/blogspot/bRuz"
            ])

(def pff (process-feeds feeds))
(def a (first pff))
(def b (second pff))
(get-words b feeds)

(defn -main
  "Cows"
  [& args]

  )
