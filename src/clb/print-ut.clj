(ns clb.print-ut)

(defn leave-only 
  "removes all keys from all clusters except k"
  [clsts k]
  (let [ths (if (k clsts) {k (k clsts)} {})]
    (if (:right clsts)
      (-> ths             ; if right exists then recursively go down
           (assoc :right (leave-only (:right clsts) k))
           (assoc :left  (leave-only (:left  clsts) k)))
       ths))) ; if this is a leaf then return it

(defn remove-pos-from-tree
  "utility function for printing that recursively constructs a new tree where no element has the 'pos' key"
  [clsts]
  (let [ths (dissoc clsts :pos)] 
    (if (:right ths)
      (-> ths
          (assoc :right (remove-pos-from-tree (:right ths)))
          (assoc :left  (remove-pos-from-tree (:left  ths))))
      ths)))

