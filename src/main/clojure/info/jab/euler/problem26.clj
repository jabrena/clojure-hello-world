(ns info.jab.euler.problem26)

(defn- better-d-cycle [[d1 c1] [d2 c2]]
  (cond
    (> c1 c2) [d1 c1]
    (< c1 c2) [d2 c2]
    :else [(max d1 d2) c1]))

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
  ;; Independent cycle-length per d; pmap + associative max-(c, tie d).
  (first
   (reduce better-d-cycle
           [0 0]
           (pmap (fn [^long d] [d (cycle-length d)]) (range 1 1000)))))
