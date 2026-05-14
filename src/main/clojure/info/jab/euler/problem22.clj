(ns info.jab.euler.problem22
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- name-score [^String s]
  (reduce + (map #(- (long %) 64) (.getBytes s "US-ASCII"))))

(defn solve []
  (let [raw (slurp (io/resource "euler/p022_names.txt"))
        names (-> raw (str/replace "\"" "") (str/split #",") (->> (map str/trim) sort vec))]
    (reduce + (map-indexed (fn [idx name] (* (inc idx) (name-score name))) names))))
