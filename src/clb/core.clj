(ns clb.core
  (:gen-class)
  (:require [clojure.pprint :as pp]
            [clb.feed-process :as fp]
            [clb.clusters :as cls]
            [clb.print-ut :as pu]))

(defn process-feeds-and-create-dataset []
  "Exactly what the name suggests"
  (let [feeds           (fp/get-feedlist)
        [all-wc ttl-wc] (fp/process-feeds feeds)
        wordlist        (fp/get-wordlist ttl-wc feeds)]
    (fp/create-dataset all-wc wordlist)))

(defn read-dataset-file []
  (fp/read-dataset))

(defn get-blogs []
  (:blogs-wc (read-dataset-file)))

(defn blogs->clusters []
  (cls/blogs->clusters (get-blogs)))

; (process-feeds-and-create-dataset) ; will process the feedlist and save important data to dataset in filesystem

(def clustered (blogs->clusters))
(def processed-space (cls/process-space clustered))
processed-space

(def no-pos (pu/remove-pos-from-tree processed-space)) ; :pos is removed from each node so it doesn't clutter the screen
(def only-blog (pu/leave-only processed-space :blog))  ; only :blog is left 


(pp/pprint only-blog) 

(defn -main
  "Cows"
  [& args]

  )
