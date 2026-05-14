(ns info.jab.euler.problem18
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- triangle []
  (mapv #(mapv parse-long (str/split % #"\s+"))
        (line-seq (io/reader (io/resource "euler/problem18_triangle.txt")))))

(defn solve []
  (let [rows (triangle)
        bottom (peek rows)
        upper (pop rows)]
    (first
     (reduce (fn [below row]
               (mapv + row (map max below (rest below))))
             bottom
             (reverse upper)))))
