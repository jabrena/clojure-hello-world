(ns easyracer.scenarios-test
  "Integration tests for easyracer scenarios.

  Boots ghcr.io/jamesward/easyracer in a Testcontainer and runs every
  scenario against it. Mirrors the parameterised test in the Java + CF
  reference implementation.

  Per-test timeouts work like Surefire's `forkedProcessTimeoutInSeconds`
  / JUnit `@Timeout`: each `deftest` is run inside a future and aborted
  after `:timeout-ms` (defaults to `default-test-timeout-ms`).
  Override per-test via `^{:timeout-ms N}` on the `deftest`."
  (:require
    [clj-test-containers.core :as tc]
    [clojure.test :as t]
    [clojure.test :refer [deftest do-report is testing use-fixtures]]
    [easyracer.scenarios :as ez]))

;; Log each scenario when it starts. `:each` fixtures run *before* `test-var`,
;; so `clojure.test/*testing-vars*` is still empty there — hook `:begin-test-var`
;; instead (fires inside `test-var`, after `*testing-vars*` is bound).
(defmethod t/report :begin-test-var [m]
  (let [v  (:var m)
        vn (some-> v meta :ns)]
    (when (= vn (find-ns 'easyracer.scenarios-test))
      (let [sym (some-> v meta :name str)
            n   (second (re-matches #"scenario-(\d+)-test" sym))
            slow? (:slow (meta v))]
        (when n
          (println (format "[easyracer] Scenario %s — %s%s"
                             n sym (if slow? " (slow)" "")))
          (flush))))))

(def ^:private easyracer-image "ghcr.io/jamesward/easyracer")

(def ^:private default-test-timeout-ms
  "Per-test cap, analogue of JUnit `@Timeout`. Most scenarios finish
   in seconds; scenario 3 (10k requests) and scenario 7 (3s hedge)
   can take longer, so override with metadata."
  60000)

(def ^:private run-timeout-ms
  "Run-wide cap, analogue of Surefire's `forkedProcessTimeoutInSeconds`.
   If the whole test run exceeds this, the JVM is force-exited with
   code 124 (matching `timeout(1)` semantics)."
  (Long/parseLong (or (System/getProperty "easyracer.run.timeout.ms")
                      "600000")))

(def ^:dynamic *base-url* nil)

(defonce ^:private container (atom nil))

(defn- start! []
  (let [c (-> (tc/create
                {:image-name    easyracer-image
                 :exposed-ports [8080]
                 :wait-for      {:wait-strategy :http
                                 :path          "/"
                                 :port          8080}})
              tc/start!)]
    (reset! container c)
    (str "http://" (:host c) ":" (get (:mapped-ports c) 8080))))

(defn- stop! []
  (when-let [c @container]
    (tc/stop! c)
    (reset! container nil)))

(defn- with-easyracer-server [test-fn]
  (let [url (start!)]
    (try
      (binding [*base-url* url]
        (test-fn))
      (finally (stop!)))))

(defn- with-run-watchdog
  "Mirrors Surefire's `forkedProcessTimeoutInSeconds`: a daemon thread
   that hard-exits the JVM if the whole test run exceeds the cap. The
   fixture interrupts the watchdog when tests finish normally."
  [test-fn]
  (let [done?    (atom false)
        watchdog (doto (Thread.
                         ^Runnable
                         (fn []
                           (try
                             (Thread/sleep run-timeout-ms)
                             (when-not @done?
                               (binding [*out* *err*]
                                 (println
                                   (format "FATAL: test run exceeded %d ms; aborting"
                                           run-timeout-ms)))
                               (System/exit 124))
                             (catch InterruptedException _ ::ok))))
                   (.setDaemon true)
                   (.setName "easyracer-test-watchdog")
                   .start)]
    (try
      (test-fn)
      (finally
        (reset! done? true)
        (.interrupt watchdog)))))

(defn- with-timeout
  "Per-test timeout fixture. Runs the test on a future, waits up to
   `:timeout-ms` (from the test var's metadata, default
   `default-test-timeout-ms`). On timeout, cancels the future and
   reports a failure to clojure.test."
  [test-fn]
  (let [v       clojure.test/*testing-vars*
        timeout (or (some-> (first v) meta :timeout-ms)
                    default-test-timeout-ms)
        fut     (future (test-fn))
        result  (deref fut timeout ::timeout)]
    (when (= ::timeout result)
      (future-cancel fut)
      (do-report
        {:type     :fail
         :message  (format "Test timed out after %d ms" timeout)
         :expected (format "completion within %d ms" timeout)
         :actual   :timeout}))))

(use-fixtures :once with-run-watchdog with-easyracer-server)
(use-fixtures :each with-timeout)

(deftest ^{:timeout-ms 30000} scenario-1-test
  (testing "Race two concurrent requests"
    (is (= :right (ez/scenario-1 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-2-test
  (testing "Race two requests, one errors"
    (is (= :right (ez/scenario-2 *base-url*)))))

(deftest ^{:timeout-ms 180000 :slow true} scenario-3-test
  (testing "Race 10000 concurrent requests"
    ;; Tagged ^:slow so it is excluded from the default test run.
    ;; Run explicitly with: clojure -M:test:test-slow
    (is (= :right (ez/scenario-3 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-4-test
  (testing "Race two requests, one with 1s timeout"
    (is (= :right (ez/scenario-4 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-5-test
  (testing "Race two requests; non-200 is a loser"
    (is (= :right (ez/scenario-5 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-6-test
  (testing "Race three requests; non-200 is a loser"
    (is (= :right (ez/scenario-6 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-7-test
  (testing "Hedging: second request after 3s"
    (is (= :right (ez/scenario-7 *base-url*)))))

(deftest ^{:timeout-ms 60000} scenario-8-test
  (testing "Resource open/use/close"
    (is (= :right (ez/scenario-8 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-9-test
  (testing "Concatenate body in response order"
    (is (= :right (ez/scenario-9 *base-url*)))))

(deftest ^{:timeout-ms 60000} scenario-10-test
  (testing "CPU work + load reporting + cancellation"
    (is (= :right (ez/scenario-10 *base-url*)))))

(deftest ^{:timeout-ms 30000} scenario-11-test
  (testing "All-failures-handled nested race"
    (is (= :right (ez/scenario-11 *base-url*)))))
