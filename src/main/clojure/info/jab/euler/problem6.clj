(ns info.jab.euler.problem6)

(defn solve []
  (let [n 100
        sum (/ (* n (inc n)) 2)
        sumsq (/ (* n (inc n) (inc (* 2 n))) 6)]
    (- (* sum sum) sumsq)))
