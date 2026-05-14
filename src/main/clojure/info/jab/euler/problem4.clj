(ns info.jab.euler.problem4
  (:require [clojure.core.reducers :as r]
            [clojure.string :as str]))

(defn- palindrome? [n]
  (let [s (str n)]
    (= s (str/reverse s))))

(defn- row-max-palindrome [^long i]
  (reduce max
          0
          (for [j (range i 1000)
                :let [p (* i j)]
                :when (palindrome? p)]
            p)))

(defn solve []
  ;; Embarrassingly parallel over rows i; combine with r/fold (fork/join).
  (r/fold
   32
   (fn ([] 0) ([a b] (max (long a) (long b))))
   (fn [acc ^long i] (max (long acc) (long (row-max-palindrome i))))
   (vec (range 100 1000))))
