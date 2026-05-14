(ns info.jab.euler.problem29)

(defn solve []
  ;; Each row a is independent; pmap then merge into a set (distinct values).
  (count
   (into #{}
         (apply concat
                (pmap (fn [^long a]
                        (for [b (range 2 101)]
                          (.pow (java.math.BigInteger/valueOf a) (int b))))
                      (range 2 101))))))
