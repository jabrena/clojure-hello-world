(ns info.jab.euler.problem19
  (:import [java.time DayOfWeek LocalDate]))

(defn solve []
  (let [start (LocalDate/of 1901 1 1)
        end (LocalDate/of 2000 12 31)]
    (count
     (filter #(and (= DayOfWeek/SUNDAY (.getDayOfWeek ^LocalDate %))
                   (== 1 (.getDayOfMonth ^LocalDate %)))
             (take-while #(not (.isAfter ^LocalDate % end))
                         (iterate #(.plusDays ^LocalDate % 1) start))))))
