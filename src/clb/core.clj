 (ns clb.core
  (:gen-class)
  (:require [clojure.string :as st]
            [clb.feed-parse :as fp]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]))

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
                    (map fp/word-count))
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
  (def fna "dtset.edn")
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
       pr-str
       (spit fna)))

(defn create-file [all-wc ttl-wc feedlist]
  (def fna "dtset.txt")
  (spit fna "Blog")
  (doseq [wrd (get-wordlist ttl-wc feedlist)]
    (spit fna (str "\t" wrd) :append true))
  )

(def feeds [ ; test feeds so we don't have to load aaaalll the feeds when testing
            "https://gigaom.com/feed/"
            "http://feeds.feedburner.com/37signals/beMH"
            "http://feeds.feedburner.com/blogspot/bRuz"
            ])

(def pff (process-feeds feeds))
(def a (first pff))
(def b (second pff))
(def wrds (get-wordlist b feeds))
;; (def p (create-dataset a wrds))


(defn -main
  "Cows"
  [& args]

  )
