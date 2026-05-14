(ns info.jab.euler.problem24)

(defn- factorial [^long n]
  (if (<= n 1)
    1
    (reduce *' (range 2 (inc n)))))

(defn- nth-lex-perm [digits ^long index]
  (loop [digits (vec (sort digits))
         index index
         acc 0N]
    (if (empty? digits)
      acc
      (let [k (dec (count digits))
            f (long (factorial k))
            i (quot index f)
            d (digits i)
            rest-d (vec (concat (subvec digits 0 i) (subvec digits (inc i))))]
        (recur rest-d (mod index f) (+ (* acc 10N) d))))))

(defn solve []
  (long (nth-lex-perm (range 10) 999999)))
