(ns info.jab.euler.problem7
  (:require [info.jab.euler.primes :as primes]))

(defn solve []
  (nth (primes/primes-up-to 200000) 10000))
