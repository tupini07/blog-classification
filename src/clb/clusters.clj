(ns clb.clusters
  (:require [clojure.string :as st]
            [clb.rss-parse :as rsp]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [clb.feed-process :as fp]))

;; We have to imagine that the space in which the blogs are placed has (count wordlist)
;; dimensions. Initially each blog will be its own cluster. The clustering algorithm will
;; iteratively find which 2 clusters are nearer and join them inside a new cluster that has
;; as a position the average of the positions of the 2 other clusters. Each cluster will have
;; a UUID

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn make-cluster [])

(defn pearson [pos1 pos2]
  "Used to calculate the closeness of 2 clusters. Pos is a map of form {dimension magnitude}"
  (let [sumf   #(apply + %)
        vals1  (vals pos1)
        vals2  (vals pos2)
        sum1   (sumf vals1)
        sum2   (sumf vals2)
        words  (keys pos1)
        sum1Sq (sumf (map #(Math/pow % 2) vals1))
        sum2Sq (sumf (map #(Math/pow % 2) vals2))
        psum   (sumf (map #(* (pos1 %) (pos2 %)) words))
        numer  (- psum 
                  (/ (* sum1 sum2)
                     (count words)))
        denom  (Math/sqrt (* (- sum1Sq 
                                (/ (Math/pow sum1 2)
                                   (count words)))
                             (- sum2Sq 
                                (/ (Math/pow sum2 2)
                                   (count words)))))]
    (if (zero? denom)
      0
      (- 1 (/ numer denom)))))

(pearson {"sado" 2 "weq" 23 "rework" 213 "rek" 324 "rweo" 231}
         {"sado" 2 "weq" 23 "rework" 213 "rek" 324 "rweo" 231})
