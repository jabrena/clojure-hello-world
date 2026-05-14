(ns info.jab.euler.euler-problems-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [info.jab.euler.problem1 :as p1]
            [info.jab.euler.problem10 :as p10]
            [info.jab.euler.problem11 :as p11]
            [info.jab.euler.problem12 :as p12]
            [info.jab.euler.problem13 :as p13]
            [info.jab.euler.problem14 :as p14]
            [info.jab.euler.problem15 :as p15]
            [info.jab.euler.problem16 :as p16]
            [info.jab.euler.problem17 :as p17]
            [info.jab.euler.problem18 :as p18]
            [info.jab.euler.problem19 :as p19]
            [info.jab.euler.problem2 :as p2]
            [info.jab.euler.problem20 :as p20]
            [info.jab.euler.problem21 :as p21]
            [info.jab.euler.problem22 :as p22]
            [info.jab.euler.problem23 :as p23]
            [info.jab.euler.problem24 :as p24]
            [info.jab.euler.problem25 :as p25]
            [info.jab.euler.problem26 :as p26]
            [info.jab.euler.problem27 :as p27]
            [info.jab.euler.problem28 :as p28]
            [info.jab.euler.problem29 :as p29]
            [info.jab.euler.problem3 :as p3]
            [info.jab.euler.problem30 :as p30]
            [info.jab.euler.problem4 :as p4]
            [info.jab.euler.problem5 :as p5]
            [info.jab.euler.problem6 :as p6]
            [info.jab.euler.problem7 :as p7]
            [info.jab.euler.problem8 :as p8]
            [info.jab.euler.problem9 :as p9])
  (:import [java.util Locale]
           [org.slf4j LoggerFactory]))

(def ^:private ^org.slf4j.Logger log
  (LoggerFactory/getLogger "info.jab.euler.euler-problems-test"))

(defn- answers-by-problem []
  (with-open [r (io/reader (io/resource "euler/answers.txt"))]
    (into {}
          (for [line (line-seq r)
                :when (seq (str/trim line))
                :let [m (re-matcher #"Problem (\d+):\s*(-?\d+)" line)]
                :when (.find m)]
            [(parse-long (.group m 1))
             (parse-long (.group m 2))]))))

(def ^:private solvers
  {1 p1/solve
   2 p2/solve
   3 p3/solve
   4 p4/solve
   5 p5/solve
   6 p6/solve
   7 p7/solve
   8 p8/solve
   9 p9/solve
   10 p10/solve
   11 p11/solve
   12 p12/solve
   13 p13/solve
   14 p14/solve
   15 p15/solve
   16 p16/solve
   17 p17/solve
   18 p18/solve
   19 p19/solve
   20 p20/solve
   21 p21/solve
   22 p22/solve
   23 p23/solve
   24 p24/solve
   25 p25/solve
   26 p26/solve
   27 p27/solve
   28 p28/solve
   29 p29/solve
   30 p30/solve})

(deftest euler-problems-1-through-30-match-published-answers
  (let [answers (answers-by-problem)]
    (doseq [n (range 1 31)]
      (let [expected (get answers n)
            solve (get solvers n)]
        (is (some? expected) (str "Missing answer line for problem " n))
        (is (some? solve) (str "No solver for problem " n))
        (when (and (some? expected) (some? solve))
          (let [t0 (System/nanoTime)
                actual (solve)
                t1 (System/nanoTime)
                elapsed-ms (/ (double (- t1 t0)) 1e6)]
            (is (= expected actual) (str "Problem " n " answer mismatch"))
            (when (= expected actual)
              (let [d (String/format Locale/US "%.3f" (object-array [elapsed-ms]))]
                (.info log "Euler problem {} passed in {} ms" n d)))))))))
