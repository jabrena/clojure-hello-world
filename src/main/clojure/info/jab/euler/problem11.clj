(ns info.jab.euler.problem11
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- grid []
  (let [raw (slurp (io/resource "euler/problem11_grid.txt"))
        nums (mapv #(Long/parseUnsignedLong %) (str/split (str/trim raw) #"\s+"))
        n 20]
    (vec (map vec (partition n nums)))))

(defn- products [g]
  (let [n (count g)
        dirs [[1 0] [-1 0] [0 1] [0 -1] [1 1] [1 -1] [-1 1] [-1 -1]]]
    (for [i (range n)
          j (range n)
          [di dj] dirs
          :let [cells (for [k (range 4)
                            :let [ni (+ i (* di k))
                                  nj (+ j (* dj k))]]
                        (get-in g [ni nj]))]
          :when (every? some? cells)]
      (reduce * cells))))

(defn solve []
  (apply max (products (grid))))
