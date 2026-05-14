(ns info.jab.euler.problem16)

(defn solve []
  (let [n (.pow (java.math.BigInteger/valueOf 2) 1000)]
    (reduce + (map #(Character/digit ^char % 10) (str n)))))
