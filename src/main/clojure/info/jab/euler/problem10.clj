(ns info.jab.euler.problem10
  (:require [info.jab.euler.primes :as primes]))

(defn solve []
  (reduce + (primes/primes-up-to 1999999)))
