(ns info.jab.euler.problem25)

(defn solve []
  (loop [i 1 a 1N b 1N]
    (if (>= (count (str a)) 1000)
      i
      (recur (inc i) b (+ a b)))))
