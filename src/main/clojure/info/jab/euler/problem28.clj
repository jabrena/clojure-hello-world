(ns info.jab.euler.problem28)

(defn solve []
  (loop [side 3 acc 1]
    (if (> side 1001)
      acc
      (recur (+ side 2) (+ acc (- (* 4 side side) (* 6 (dec side))))))))
