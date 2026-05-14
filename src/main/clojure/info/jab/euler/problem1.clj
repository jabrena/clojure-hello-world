(ns info.jab.euler.problem1)

(defn- multiple-of-3-or-5? [n]
  (or (zero? (mod n 3))
      (zero? (mod n 5))))

(defn sum-of-multiples-below [limit]
  (transduce (filter multiple-of-3-or-5?) + (range limit)))

(defn solve []
  (sum-of-multiples-below 1000))
