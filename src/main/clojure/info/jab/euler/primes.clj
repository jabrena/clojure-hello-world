(ns info.jab.euler.primes)

(defn prime? [^long n]
  (cond (< n 2) false
        (== n 2) true
        (even? n) false
        :else (not-any? #(zero? (mod n %))
                         (range 3 (inc (long (Math/sqrt n))) 2))))

(defn primes-up-to
  "Returns a lazy seq of primes <= limit (inclusive)."
  [limit]
  (when (>= limit 2)
    (let [^booleans sieve (boolean-array (inc limit) true)]
      (aset sieve 0 false)
      (aset sieve 1 false)
      (doseq [p (range 2 (inc (int (Math/sqrt limit))))
              :when (aget sieve p)
              i (range (* p p) (inc limit) p)]
        (aset sieve i false))
      (filter #(aget sieve %) (range 2 (inc limit))))))
