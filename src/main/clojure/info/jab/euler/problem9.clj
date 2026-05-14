(ns info.jab.euler.problem9)

(defn solve []
  (first
   (for [a (range 1 1000)
         b (range (inc a) (- 1000 a))
         :let [c (- 1000 a b)]
         :when (and (> c b) (= (+ (* a a) (* b b)) (* c c)))]
     (* a b c))))
