(ns info.jab.euler.problem5)

(defn- gcd [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn- lcm [a b]
  (/ (* a b) (gcd a b)))

(defn solve []
  (reduce lcm (range 1 21)))
