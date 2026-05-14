(ns info.jab.euler.problem20)

(defn- factorial [n]
  (reduce *' (range 2 (inc n))))

(defn solve []
  (reduce + (map #(Character/digit ^char % 10) (str (factorial 100)))))
