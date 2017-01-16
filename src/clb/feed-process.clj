(ns clb.feed-process
  (:require [clojure.string :as st]
            [clb.rss-parse :as rsp]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]))

; Name of the dataset
(def fna "dtset.edn")

(defn rsrc-path [fname]
  "Returns path to resource folder"
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
  "applies rsp/word-count to each feed in the list and returns the result of this
   concatenated with the total wordcount of all processed feeds"
  (let [all-wc (->> feeds ; Construct map consisting of all titles and their wc 
                    (map rsp/word-count))
        ttl-wc (->> (map :wc all-wc) ; get all wc maps 
                    (apply merge-with +))] ; merge all wc maps with +
    [all-wc ttl-wc]))


(defn get-wordlist [total-wc feedlist]
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


(defn create-dataset [all-wc wordlist]
  "Gets the result of applying fp/word-count to each feed and the wordlist
   to use. Filters out only words in the wordlist and saves all to an edn file."
  (->> all-wc 
       (map (fn [blg]
              (def blog (:title blg))
              (def wc (:wc blg))
              (->> (map (fn [word]                                  
                          {word (get wc word 0)})
                        wordlist) ; construct array of all words in wordlist
                   (apply merge)
                   (#(identity {:blog blog
                                :wc   %})))))
                                        ; now we have an array of blogs with their respective wordcount we can add to the edn file
                                        ; all the words in the wordcounts are the same and only the counts change so we can use
                                        ; each of this words as a dimension in the place we will put our blogs to compare their 
                                        ; nearness
       
       (#(identity {:wordlist wordlist
                    :blogs-wc %}))
       pr-str ; convert to string
       (spit (rsrc-path fna))))   ; write to file

(defn read-dataset []
  "returns a map with the following form {:wordlist [wordlist that is being used]
                                          :bogs-wc [{:blog blog :wc wc}]}"
  (read-string (slurp (rsrc-path fna))))


(def feeds [ ; test feeds so we don't have to load aaaalll the feeds when testing
            "https://gigaom.com/feed/"
            "http://feeds.feedburner.com/37signals/beMH"
            "http://feeds.feedburner.com/blogspot/bRuz"
            ])

;; (read-dataset)
(def pff (process-feeds feeds))
(def a (first pff))
(def b (second pff))
(def wrds (get-wordlist b feeds))
;; (def p (create-dataset a wrds))

