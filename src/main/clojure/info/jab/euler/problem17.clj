(ns info.jab.euler.problem17)

(def ^:private ones
  ["" "one" "two" "three" "four" "five" "six" "seven" "eight" "nine"])

(def ^:private teens
  ["ten" "eleven" "twelve" "thirteen" "fourteen" "fifteen" "sixteen"
   "seventeen" "eighteen" "nineteen"])

(def ^:private tens
  ["" "" "twenty" "thirty" "forty" "fifty" "sixty" "seventy" "eighty" "ninety"])

(defn- under-100 [^long n]
  (cond (< n 10) (ones n)
        (< n 20) (teens (- n 10))
        :else (let [t (quot n 10) u (mod n 10)]
                (str (tens t) (ones u)))))

(defn- under-1000 [^long n]
  (let [h (quot n 100) r (mod n 100)]
    (str (when (pos? h) (str (ones h) "hundred"))
         (when (and (pos? h) (pos? r)) "and")
         (when (pos? r) (under-100 r)))))

(defn- letters [^long n]
  (count
   (if (== n 1000)
     "onethousand"
     (under-1000 n))))

(defn solve []
  (reduce + (map letters (range 1 1001))))
