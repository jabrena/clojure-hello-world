(ns info.jab.euler.problem4
  (:require [clojure.string :as str]))

(defn- palindrome? [n]
  (let [s (str n)]
    (= s (str/reverse s))))

(defn solve []
  (reduce max
          (for [i (range 100 1000)
                j (range i 1000)
                :let [p (* i j)]
                :when (palindrome? p)]
            p)))
