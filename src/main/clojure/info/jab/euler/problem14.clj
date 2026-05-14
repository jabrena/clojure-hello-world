(ns info.jab.euler.problem14
  (:import [java.util ArrayList HashMap Map]))

(defn- collatz-next [^long x]
  (if (even? x) (quot x 2) (unchecked-inc (* 3 x))))

(defn solve []
  (let [^Map mem (HashMap.)]
    (.put mem 1 1)
    (doseq [^long n (range 1 1000000)]
      (when-not (.containsKey mem n)
        (loop [^long cur n ^ArrayList path (ArrayList.)]
          (if (.containsKey mem cur)
            (let [^long base (.get mem cur)
                  sz (.size path)]
              (dotimes [i sz]
                (.put mem (.get path i) (+ (- sz i) base))))
            (do (.add path cur)
                (recur (collatz-next cur) path))))))
    (first (apply max-key second (for [^long i (range 1 1000000)] [i (.get mem i)])))))
