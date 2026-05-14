(ns info.jab.euler.problem29)

(defn solve []
  (count (into #{} (for [a (range 2 101)
                         b (range 2 101)]
                     (.pow (java.math.BigInteger/valueOf a) (int b))))))
