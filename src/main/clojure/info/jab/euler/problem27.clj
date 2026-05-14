(ns info.jab.euler.problem27
  (:require [info.jab.euler.primes :as primes]))

(defn- poly-prime-run [^long a ^long b]
  (loop [n 0]
    (let [v (+ (* n n) (* a n) b)]
      (if (and (> v 1) (primes/prime? v))
        (recur (inc n))
        n))))

(defn solve []
  (second
   (reduce (fn [[best-run prod] [a b]]
             (let [r (poly-prime-run a b)]
               (if (> r best-run) [r (* a b)] [best-run prod])))
           [0 0]
           (for [a (range -999 1000)
                 b (range -999 1000)]
             [a b]))))
