# clojure-hello-world

Minimal Clojure “Hello, World!” with a root [`deps.edn`](https://clojure.org/reference/deps_and_cli) for the Clojure CLI.

This repo also includes a Clojure implementation of the [EasyRacer](https://github.com/jamesward/easyracer) scenarios under `info.jab.easyracer.scenarios`, built on:

- [`babashka.http-client`](https://github.com/babashka/http-client) — Clojure HTTP client used by the EasyRacer scenario implementation.
- [`org.clojure/core.async`](https://github.com/clojure/core.async) — channels and `alts!` for racing concurrent requests.
- [`clj-test-containers`](https://github.com/javahippie/clj-test-containers) — boots `ghcr.io/jamesward/easyracer` for integration tests.

Sources live under `src/main/clojure/`. Tests live under `src/test/clojure/`.

## Prerequisites (Java 21)

Use **JDK 21** for this project.

1. **Use SDKMAN** with the checked-in `.sdkmanrc`:

   ```bash
   sdk env install
   sdk env
   java -version
   ```

   This repo pins `java=21.0.2-graalce`, and `java -version` should show a Java 21 runtime.

2. **Clojure CLI** (`clojure` on your `PATH`) is required for local runs and test commands. Install the CLI from the [official getting started guide](https://clojure.org/guides/install_clojure).

```bash
brew install clojure/tools/clojure
```

## EasyRacer

The project currently verifies EasyRacer scenarios through the integration test suite (no standalone CLI entrypoint in `info.jab.easyracer.scenarios`).

### Tests (Testcontainers)

Docker must be running; the test fixture starts/stops the server automatically.

```bash
clojure -M:test                 # all scenarios, including scenario 3
```

#### Run tests locally

Local test run checklist:

```bash
# 1) Ensure Docker is available
docker ps

# 2) Run the default integration suite locally
clojure -M:test
```

Test timeouts (analogue of Surefire's `forkedProcessTimeoutInSeconds` and JUnit `@Timeout`):

- Per-test cap, set via metadata: `^{:timeout-ms 30000}` on each `deftest`. Default 60 s.
- Run-wide cap, hard-exits the JVM if exceeded:

  ```bash
  clojure -J-Deasyracer.run.timeout.ms=300000 -M:test
  ```

## Docker

Prepared to test in OSX:

```bash
docker compose build verify
docker compose build --no-cache verify
docker compose run --rm verify
```

The `verify` container runs the full test suite from the `Dockerfile` `CMD` with `clojure -M:test` (including scenario 3).

## Layout

```text
├── deps.edn
└── src/
    ├── main/
    │   └── clojure/
    │       ├── hello_world/
    │       │   └── core.clj
    │       └── info/
    │           └── jab/
    │               └── easyracer/
    │                   ├── http.clj
    │                   └── scenarios.clj
    └── test/
        └── clojure/
            └── info/
                └── jab/
                    └── easyracer/
                        └── scenarios_test.clj
```

The namespaces are `hello-world.core` and `info.jab.easyracer.scenarios` (with tests in `info.jab.easyracer.scenarios-test`).
