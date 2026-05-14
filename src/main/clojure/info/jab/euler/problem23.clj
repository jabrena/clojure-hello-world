(ns info.jab.euler.problem23)

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
  (let [limit 28123
        abundant (boolean-array (inc limit))]
    (doseq [n (range 1 (inc limit))
            :when (> (proper-divisor-sum n) n)]
      (aset abundant n true))
    (let [writable (boolean-array (inc limit))]
      (doseq [i (range 1 (inc limit))
              :when (aget abundant i)
              j (range i (inc limit))
              :when (aget abundant j)
              :let [s (+ i j)]
              :when (<= s limit)]
        (aset writable s true))
      (reduce + (remove #(aget writable %) (range 1 limit))))))
