(ns info.jab.euler.problem15)

(defn- binomial [^long n ^long k]
  (loop [i 1 acc 1N]
    (if (> i k)
      acc
      (recur (inc i) (/ (* acc (+ (- n k) i)) (bigint i))))))

(defn solve []
  (long (binomial 40 20)))
