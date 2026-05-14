(ns info.jab.euler.problem21)

(defn- proper-divisor-sum [^long n]
  (if (== n 1)
    0
    (loop [d 2 s 1 lim (long (Math/sqrt n))]
      (if (> d lim)
        s
        (if (zero? (mod n d))
          (let [q (quot n d)]
            (recur (inc d)
                   (cond-> s
                     true (+ d)
                     (not= d q) (+ q))
                   lim))
          (recur (inc d) s lim))))))

(defn solve []
  (let [limit 10000
        d (long-array (inc limit))]
    (doseq [a (range 1 limit)]
      (aset d a (proper-divisor-sum a)))
    (reduce +
            (for [a (range 1 limit)
                  :let [b (aget d a)]
                  :when (and (not= a b)
                              (< b limit)
                              (== a (aget d b)))]
              a))))
