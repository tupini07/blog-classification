(ns clb.clusters
  (:require [clojure.string :as st]
            [clojure.math.combinatorics :as comb]))

;; We have to imagine that the space in which the blogs are placed has (count wordlist)
;; dimensions. Initially each blog will be its own cluster. The clustering algorithm will
;; iteratively find which 2 clusters are nearer and join them inside a new cluster that has
;; as a position the average of the positions of the 2 other clusters. Each cluster will have
;; a UUID

(defn uuid [] (str (java.util.UUID/randomUUID)))

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


(defn make-cluster 
  "Imagine clusters as a tree where each node has 2 sub-leafs
   Example: cluster of only 2 blogs
    -> root ; cluster that contains all other nodes
       -> cluster1 -> blog-1
       -> cluster2 -> blog-2 "
  ([nme pos] (merge {:blog nme} ; only leafs have blog names
                    (make-cluster pos nil nil))) ; a leaf
  ([pos left right]
   {:id (uuid)
    :pos pos
    :left left
    :right right}))

(defn blogs->clusters 
  "recieves an array of maps {:blog title :wc position} and returns an
   array of clusters"
  [blogs]
  (->> blogs
       (map #(make-cluster (:blog %) (:wc %)))))

(defn calc-dists
  "calculate the distances between all elements in space (all clusters)
     space is an array of all clusters"
  [space]
  (defn single-distances 
    "calculate distance between 2 clusters"
    [cl1 cl2]
    {:ids  #{(:id cl1) (:id cl2)} 
     :dist (pearson (:pos cl1) (:pos cl2))})

  ;; this could be more efficient but for ocmprehentions sake..
  (let [poss-c (comb/combinations space)] ;; array of pairs of all possible combinations
    (->> poss-c
         (map (fn [[cl1 cl2]]
                (single-distances cl1 cl2))))))

(defn find-min-dist 
  "Gets the already processed clusters and finds the pair with the least distance"
  [prc-clsts]
  (apply min-key :dist prc-clsts))
