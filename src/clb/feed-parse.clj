(ns clb.feed-parse
  (:use feedparser-clj.core)
  (:require [clojure.string :as st]))

;; (def ptf (parse-feed "https://gigaom.com/feed/"))
;; (def tent (:entries ptf))
;; (keys ptf)
;; (keys (first tent))
;; (map :description (:entries ptf))

(defn get-words [html]
  "Strips out HTML and returns only the alpha character in lowecase"
  (->> (-> html
           (st/replace #"<[^>]+>" "") ; Remove HTML characters
           (st/split #"[^A-Z^a-z]+")) ; Split in non-alpha characters
       (filter #(not= % ""))          ; Filter out empty strings
       (map st/lower-case)))          ; Convert all to lower-case

(defn word-count [url]
  "given a URL of a feed it returns [{:title entry-title 
                                      :words-counts {:word1 cnt1 ...}]"
  (->> (parse-feed url)
       :entries
       (map (fn [ent] ;; do the following proces once for each entry
           ; if a summary of the entry exists then we use the entry to do our word count
           ; if none exists then we use the entrie's description
              (let [ttle (:title ent)
                    txt  (-> (if-let [smr (:summary ent)] 
                               smr
                               (:description ent))
                             :value ; get the value of the field
                             (str " " ttle)
                             get-words)
                    rf   (fn [mp wrd]
                           (if-not (contains? mp wrd) ; if map still doesn't contains the word
                             (assoc mp wrd 1) ; associate value of word with 1
                             (assoc mp wrd (inc (mp wrd)))))] ; increment value of word
                {:title ttle
                 :wc    (reduce rf {} txt)})))))

(word-count "https://gigaom.com/feed/")
