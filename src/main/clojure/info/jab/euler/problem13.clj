(ns info.jab.euler.problem13
  (:require [clojure.java.io :as io]))

(defn solve []
  (let [lines (line-seq (io/reader (io/resource "euler/problem13_numbers.txt")))
        sum (reduce +' (map bigint lines))
        s (str sum)]
    (parse-long (subs s 0 10))))
