(ns info.jab.euler.problem3)

(defn- largest-prime-factor [n]
  (loop [n n
         d 2
         m 1]
    (cond
      (= n 1) m
      (> (* d d) n) (max m n)
      (zero? (mod n d)) (recur (quot n d) d (max m d))
      :else (recur n (if (= d 2) 3 (+ d 2)) m))))

(defn solve []
  (largest-prime-factor 600851475143))
