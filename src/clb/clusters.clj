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
  (let [poss-c (comb/combinations space 2)] ;; array of pairs of all possible combinations
    (->> poss-c
         (map (fn [[cl1 cl2]]
                (single-distances cl1 cl2))))))


(defn find-min-dist 
  "Gets the already processed clusters and finds the pair with the least distance"
  [prc-clsts]
  (apply min-key :dist prc-clsts))

(defn get-by-id
  "Given the ID of a cluster and the cluster space returns the cluster with said ID
   that is in the top level of the space."
  [id space]
  (some #(when (= (:id %) id) %) space))

(defn remove-by-id
  "given a set of ids and a space remove cluster with said ID"
  [space ids]
  (filter #(not (contains? ids (:id %))) space))

(defn average-positions
  "returns a new pos map where the value of each dimension is the average of said dimension
   in both passed positions. Supposes that pos1 and pos2 both specify the same dimensions.
     e:  (= (keys pos1) (keys pos2))"
  [pos1 pos2]
  (->> (keys pos1)
       (map (fn [k] 
              {k (/ 
                  (+ (pos1 k) (pos2 k)) 
                  2)}))
       (apply merge)))

(defn merge-clusters
  ([id1 id2 space]
   "Gets 2 cluster ids, finds them in space, merges them into new cluster and returns it"
   (merge-clusters (get-by-id id1 space)
                   (get-by-id id2 space)))
  ([c1 c2]
   "Merges 2 clusters into new cluster and returns it"
   (let [new-pos (average-positions (:pos c1) (:pos c2))]
     (make-cluster new-pos ; new position for this cluster
                   c1      ; cluster's left branch
                   c2))))  ; cluster's right branch

(defn merge-and-subst 
  "Gets 2 clusters and the space. Merges this 2 clusters, removes them from space and in their place adds the merged one."
  [space c1 c2]
  (let [c1      (if (:id c1) c1 (get-by-id c1 space)) ; If c1 doesn't contain :id element then c1 IS the ID.
        c2      (if (:id c2) c2 (get-by-id c2 space))
        merged  (merge-clusters c1 c2) ; the result of merging c1 and c2
        ids     #{(:id c1) (:id c2)}
        space-r (remove-by-id space ids)] ; space without c1 and c2
    (conj space-r merged))) ; add merged to new space and return

(defn process-space 
  "gets a space array of clusters and process loops on it until there is only one cluster left"
  [clsts]
  (if (or (= (count clsts) 1)
          (empty? clsts))
    (first clsts) ; initially this was actually an array but what we want is the tree's root
    (let [distances    (calc-dists clsts)
          nearest-pair (:ids (find-min-dist distances))
          [idc1 idc2]  (seq nearest-pair)]
      (recur (merge-and-subst clsts idc1 idc2)))))

