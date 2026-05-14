(ns info.jab.euler.problem26)

(defn- cycle-length [^long d]
  (let [d' (loop [x d]
             (cond (zero? (mod x 2)) (recur (quot x 2))
                   (zero? (mod x 5)) (recur (quot x 5))
                   :else x))]
    (if (== d' 1)
      0
      (loop [len 1 p (mod 10 d')]
        (if (== p 1)
          len
          (recur (inc len) (mod (* p 10) d')))))))

(defn solve []
  (first
   (reduce (fn [[best-d best-c] ^long d]
             (let [c (cycle-length d)]
               (if (>= c best-c) [d c] [best-d best-c])))
           [0 0]
           (range 1 1000))))
