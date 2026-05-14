(ns info.jab.euler.problem30)

(def ^:private fifth
  (vec (for [d (range 10)] (long (Math/pow d 5)))))

(defn- digit-fifth-sum [n]
  (reduce + (map #(fifth (Character/digit ^char % 10)) (str n))))

(defn solve []
  (reduce + (for [n (range 2 (* 6 (fifth 9)))
                  :when (== n (digit-fifth-sum n))]
              n)))
