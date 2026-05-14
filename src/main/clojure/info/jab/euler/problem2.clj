(ns info.jab.euler.problem2)

(defn solve []
  (loop [f1 1
         f2 2
         acc 0]
    (if (>= f2 4000000)
      acc
      (recur f2 (+ f1 f2) (if (even? f2) (+ acc f2) acc)))))
