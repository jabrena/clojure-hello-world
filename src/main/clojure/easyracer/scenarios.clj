(ns easyracer.scenarios
  "EasyRacer client implementation using HATO + core.async.

  See https://github.com/jamesward/easyracer for the scenario specs.

  Each scenario function takes a base URL string and returns either
  :right (success) or :left (failure)."
  (:require
    [clojure.core.async :as async]
    [hato.client :as http])
  (:import
    (java.net.http HttpClient)
    (java.security MessageDigest)
    (java.time Instant)
    (java.util Random)
    (java.util.concurrent CompletableFuture Executors TimeUnit)
    (java.util.function BiConsumer))
  (:gen-class))

;; ---------------------------------------------------------------------------
;; Shared infrastructure
;; ---------------------------------------------------------------------------

(def ^:private virtual-executor
  "A virtual-thread executor used for HATO's async work and for tasks
   like the SHA-512 loop in scenario 10. Sized for thousands of
   concurrent racers (scenario 3 fires 10k requests)."
  (delay
    (Executors/newThreadPerTaskExecutor
      (-> (Thread/ofVirtual)
          (.name "easyracer-vt-" 0)
          .factory))))

(def ^:private http-client
  "Single shared HATO/JDK HttpClient. HTTP/2 multiplexing is the
   default; we wire in the virtual-thread executor so 10k concurrent
   requests don't exhaust the platform thread pool."
  (delay
    (http/build-http-client
      {:version :http-2
       :executor @virtual-executor
       :connect-timeout 10000})))

(defn- right? [v] (= :right v))

(defn- response->value
  "Maps a HATO response map to :right when status==200 and body=='right',
   otherwise :left."
  [{:keys [status body]}]
  (if (and (= 200 status) (= "right" body))
    :right
    :left))

(defn- cf->chan
  "Adapts a CompletableFuture<HttpResponse-like-map> to a core.async
   channel that always delivers exactly one value (:right/:left), then
   closes. Exceptions and non-200 responses become :left."
  [^CompletableFuture cf]
  (let [c (async/chan 1)]
    (.whenComplete cf
      (reify BiConsumer
        (accept [_ result ex]
          (let [v (cond
                    ex     :left
                    result (response->value result)
                    :else  :left)]
            (async/put! c v)
            (async/close! c)))))
    c))

(defn- async-get
  "Issues an asynchronous GET and returns the underlying CompletableFuture.
   Extra HATO opts (e.g. :timeout) can be passed in."
  ([url] (async-get url {}))
  ([url opts]
   (http/get url
     (merge {:async? true
             :http-client @http-client
             :throw-exceptions? false}
            opts))))

(defn- race-futures
  "Race a collection of CompletableFutures. The first one that yields
   :right wins; if none do, the result is :left. Losing futures are
   cancelled so their HTTP connections actually close (important for
   the EasyRacer server, which only releases the winner once the
   losers are gone)."
  [futures]
  (let [futures (vec futures)
        chans   (mapv cf->chan futures)
        winner  (async/<!!
                  (async/go-loop [pending (vec (map vector futures chans))]
                    (if (empty? pending)
                      :left
                      (let [pending-chans (mapv second pending)
                            [v ch] (async/alts! pending-chans)]
                        (if (right? v)
                          :right
                          (recur (filterv #(not= (second %) ch) pending)))))))]
    (doseq [^CompletableFuture cf futures]
      (when-not (.isDone cf)
        (.cancel cf true)))
    winner))

;; ---------------------------------------------------------------------------
;; Scenarios
;; ---------------------------------------------------------------------------

(defn scenario-1
  "Race two concurrent requests; the winner returns 'right'."
  [base-url]
  (race-futures
    [(async-get (str base-url "/1"))
     (async-get (str base-url "/1"))]))

(defn scenario-2
  "Race two requests; one of them errors out."
  [base-url]
  (race-futures
    [(async-get (str base-url "/2"))
     (async-get (str base-url "/2"))]))

(defn scenario-3
  "Race 10 000 concurrent requests."
  [base-url]
  (let [url (str base-url "/3")
        futures (mapv (fn [_] (async-get url {:timeout 120000}))
                      (range 10000))]
    (race-futures futures)))

(defn scenario-4
  "Race two requests; one of them must time out after 1s. The short
   request is given a 1s :timeout so the underlying connection is
   actually closed when it expires."
  [base-url]
  (race-futures
    [(async-get (str base-url "/4") {:timeout 1000})
     (async-get (str base-url "/4"))]))

(defn scenario-5
  "Race two requests; non-200 is a loser."
  [base-url]
  (race-futures
    [(async-get (str base-url "/5"))
     (async-get (str base-url "/5"))]))

(defn scenario-6
  "Race three requests; non-200 is a loser."
  [base-url]
  (race-futures
    [(async-get (str base-url "/6"))
     (async-get (str base-url "/6"))
     (async-get (str base-url "/6"))]))

(defn scenario-7
  "Hedging: start a request, wait at least 3 s, then start a second one."
  [base-url]
  (let [url (str base-url "/7")
        first-cf  (async-get url)
        ;; Build a CompletableFuture that fires the second call
        ;; after a 3-second delay using the JDK delayedExecutor.
        delayed-exec (CompletableFuture/delayedExecutor
                       3 TimeUnit/SECONDS @virtual-executor)
        second-cf (-> (CompletableFuture/supplyAsync
                        ^java.util.function.Supplier
                        (reify java.util.function.Supplier
                          (get [_] (async-get url)))
                        delayed-exec)
                      (.thenCompose
                        (reify java.util.function.Function
                          (apply [_ cf] cf))))]
    (race-futures [first-cf second-cf])))

(defn scenario-8
  "Resource management: open -> use -> close. Race two such flows."
  [base-url]
  (letfn [(resource-flow []
            (let [open-cf (async-get (str base-url "/8?open"))]
              (-> open-cf
                  (.thenCompose
                    (reify java.util.function.Function
                      (apply [_ open-resp]
                        (let [resource-id (:body open-resp)
                              use-cf  (async-get (str base-url "/8?use=" resource-id))
                              ;; Always close, regardless of use outcome.
                              close!  (fn [_]
                                        (async-get (str base-url "/8?close=" resource-id)))]
                          (-> use-cf
                              (.whenComplete
                                (reify BiConsumer
                                  (accept [_ _r _ex]
                                    (close! resource-id)))))))))))) ]
    (race-futures [(resource-flow) (resource-flow)])))

(defn scenario-9
  "Make 10 concurrent requests; 5 return 200 with a single letter.
   Concatenated in response-arrival order they spell 'right'."
  [base-url]
  (let [url (str base-url "/9")
        cfs (mapv (fn [_]
                    (-> (http/get url {:async?            true
                                       :http-client       @http-client
                                       :throw-exceptions? false})
                        (.thenApply
                          (reify java.util.function.Function
                            (apply [_ resp]
                              {:at   (Instant/now)
                               :resp resp})))))
                  (range 10))
        ;; Wait for all to complete.
        results (->> cfs
                     (mapv #(.join ^CompletableFuture %))
                     (filter #(= 200 (:status (:resp %))))
                     (sort-by :at)
                     (map #(:body (:resp %)))
                     (apply str))]
    (if (= "right" results) :right :left)))

(defn scenario-10
  "Run a CPU-heavy task in parallel with a load-reporting loop, then
   cancel when the blocker connection closes.

   Part 1: open /10?<id>; while it is open, hash SHA-512 in a tight loop.
   Part 2: every second, POST current process load to /10?<id>=<load>.
           2xx => done, 3xx => keep polling, 4xx => failure."
  [base-url]
  (let [id        (str (random-uuid))
        cancelled (volatile! false)
        ;; OperatingSystemMXBean is the Sun-specific extended bean.
        os-bean   (java.lang.management.ManagementFactory/getPlatformMXBean
                    com.sun.management.OperatingSystemMXBean)
        cpus      (.availableProcessors (Runtime/getRuntime))]
    ;; Part 1a: SHA loop on a virtual thread.
    (.execute ^java.util.concurrent.Executor @virtual-executor
      (fn []
        (let [md (MessageDigest/getInstance "SHA-512")
              buf (byte-array 512)]
          (.nextBytes (Random.) buf)
          (loop [b buf]
            (if @cancelled
              :done
              (recur (.digest md b)))))))
    ;; Part 1b: blocker request - keep connection open then signal cancel.
    (let [blocker-cf (async-get (str base-url "/10?" id))
          _ (.whenComplete blocker-cf
              (reify BiConsumer
                (accept [_ _r _ex]
                  (vreset! cancelled true))))
          ;; Part 2: load-reporting loop until 2xx/4xx.
          poll (fn []
                 (loop []
                   (let [load (* (.getProcessCpuLoad os-bean) cpus)
                         {:keys [status body]}
                         (try
                           (http/get (str base-url "/10?" id "=" load)
                             {:http-client       @http-client
                              :throw-exceptions? false})
                           (catch Exception _ {:status 500 :body ""}))]
                     (cond
                       (and (>= status 200) (< status 300))
                       (do (vreset! cancelled true)
                           (if (= "right" body) :right :left))

                       (and (>= status 300) (< status 400))
                       (do (Thread/sleep 1000) (recur))

                       :else
                       (do (vreset! cancelled true) :left)))))
          result (poll)]
      ;; Make sure the blocker is fully done so the server knows the loser left.
      (try (.join blocker-cf) (catch Exception _))
      result)))

(defn scenario-11
  "All-failures-handled race: race a single request against a nested
   race of two requests."
  [base-url]
  (let [url (str base-url "/11")
        inner-future (CompletableFuture/supplyAsync
                       ^java.util.function.Supplier
                       (reify java.util.function.Supplier
                         (get [_]
                           (let [v (race-futures
                                     [(async-get url) (async-get url)])]
                             ;; race-futures returns :right/:left; we need
                             ;; to feed the outer race a "response-shaped"
                             ;; map so cf->chan can interpret it.
                             (if (right? v)
                               {:status 200 :body "right"}
                               {:status 500 :body ""}))))
                       ^java.util.concurrent.Executor @virtual-executor)]
    (race-futures
      [inner-future
       (async-get url)])))

;; ---------------------------------------------------------------------------
;; Entry point - run every scenario from the CLI for ad-hoc verification.
;; ---------------------------------------------------------------------------

(def all-scenarios
  [["1"  scenario-1]
   ["2"  scenario-2]
   ["3"  scenario-3]
   ["4"  scenario-4]
   ["5"  scenario-5]
   ["6"  scenario-6]
   ["7"  scenario-7]
   ["8"  scenario-8]
   ["9"  scenario-9]
   ["10" scenario-10]
   ["11" scenario-11]])

(defn -main
  "Usage: clojure -M -m easyracer.scenarios [base-url]
   Default base-url is http://localhost:8080."
  [& args]
  (let [base-url (or (first args) "http://localhost:8080")]
    (println "EasyRacer client - base URL:" base-url)
    (doseq [[label f] all-scenarios]
      (let [start (System/currentTimeMillis)
            v     (try (f base-url) (catch Throwable t
                                      (println "  ERROR" t) :left))
            took  (- (System/currentTimeMillis) start)]
        (println (format "Scenario %2s: %-6s (%d ms)"
                         label (name v) took))))
    (shutdown-agents)))
