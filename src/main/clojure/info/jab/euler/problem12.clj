(ns info.jab.euler.problem12)

(defn- count-divisors [^long n]
  (let [lim (long (Math/sqrt n))]
    (loop [d 1 c 0]
      (if (> d lim)
        c
        (if (zero? (mod n d))
          (let [q (quot n d)]
            (recur (inc d) (if (= d q) (inc c) (+ c 2))))
          (recur (inc d) c))))))

(defn solve []
  (loop [i 1]
    (let [t (quot (* i (inc i)) 2)]
      (if (> (count-divisors t) 500)
        t
        (recur (inc i))))))
