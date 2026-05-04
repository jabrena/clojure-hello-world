# clojure-hello-world

Minimal Clojure “Hello, World!” using [Apache Maven](https://maven.apache.org/) and [vivid `clojure-maven-plugin`](https://github.com/vivid-inc/clojure-maven-plugin), plus a root [`deps.edn`](https://clojure.org/reference/deps_and_cli) for the Clojure CLI.

This repo also includes a Clojure implementation of the [EasyRacer](https://github.com/jamesward/easyracer) scenarios under `easyracer.scenarios`, built on:

- [`hato`](https://github.com/gnarroway/hato) — Clojure HTTP client wrapping JDK 11+ `HttpClient` (HTTP/2 + virtual-thread executor).
- [`org.clojure/core.async`](https://github.com/clojure/core.async) — channels and `alts!` for racing concurrent requests.
- [`clj-test-containers`](https://github.com/javahippie/clj-test-containers) — boots `ghcr.io/jamesward/easyracer` for integration tests.

Sources live under `src/main/clojure/` (Maven-style layout). Tests live under `src/test/clojure/`.

## Prerequisites (Java 21)

Use **JDK 21** for this project (`maven.compiler.release` and the Enforcer plugin both target Java 21).

1. **Install JDK 21** and point your environment at it, for example:

   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
   java -version
   ```

   You should see a `21` feature release in the output.

2. **Apache Maven** 3.9+ is recommended. Confirm Maven is using Java 21:

   ```bash
   mvn -v
   ```

   The first line of the output should show `Java version: 21...`.

3. **Clojure CLI** (`clojure` on your `PATH`) is required for the vivid `clojure` goals (`run`, `easyracer`), which invoke `clojure` with the Maven classpath. EasyRacer integration tests driven by **`mvn test`** use `java … clojure.main` with a classpath built by `maven-dependency-plugin`, so the Clojure CLI is optional for that step (only a JDK is required). Install the CLI from the [official getting started guide](https://clojure.org/guides/install_clojure).

```bash
brew install clojure/tools/clojure
```

## Build with Maven

```bash
mvn vivid:clojure-maven-plugin:0.3.0:clojure@run
```

Maven does not resolve the short `vivid:` plugin prefix for this artifact, so the goal is invoked with the `groupId:artifactId:version:goal` form as above.

## Regenerate `deps.edn` from the POM

The plugin can emit a `deps.edn` that mirrors each vivid `clojure` execution in `pom.xml`:

```bash
mvn vivid:clojure-maven-plugin:0.3.0:deps.edn
```

That overwrites `deps.edn` in the project root. The committed file is hand-tuned for the Clojure CLI (`:main-opts` as separate strings). After regenerating, compare with `git diff` and adjust if you rely on `clojure -M:run`.

## EasyRacer

Run all 11 scenarios against a local EasyRacer server (skip slow scenario 3 by default):

```bash
docker run -d --rm -p 8080:8080 ghcr.io/jamesward/easyracer
clojure -M:easyracer            # uses http://localhost:8080
```

### Tests (Testcontainers)

Docker must be running; the test fixture starts/stops the server automatically.

```bash
clojure -M:test                 # all scenarios except :slow (skips scenario 3)
clojure -M:test:test-slow       # only the :slow scenarios (scenario 3, 10k requests)
./mvnw -B -ntp test             # same integration suite as above (fixtures + Testcontainers)
```

Do **not** use `vivid:clojure-maven-plugin:…:clojure@test` for these tests: that goal resolves dependencies through the plugin’s own Aether session and only sees Maven Central, so Clojars artifacts (`hato`, `clj-test-containers`) fail to resolve. The `test` phase uses `exec-maven-plugin` plus `dependency:build-classpath`, which honours this POM’s `<repositories>` (including [Clojars](https://repo.clojars.org/)).

Test timeouts (analogue of Surefire's `forkedProcessTimeoutInSeconds` and JUnit `@Timeout`):

- Per-test cap, set via metadata: `^{:timeout-ms 30000}` on each `deftest`. Default 60 s.
- Run-wide cap, hard-exits the JVM if exceeded:

  ```bash
  clojure -J-Deasyracer.run.timeout.ms=300000 -M:test
  mvn ... -Deasyracer.run.timeout.ms=300000
  ```

## Layout

```text
├── deps.edn
├── pom.xml
└── src/
    ├── main/
    │   └── clojure/
    │       ├── hello_world/
    │       │   └── core.clj
    │       └── easyracer/
    │           └── scenarios.clj
    └── test/
        └── clojure/
            └── easyracer/
                └── scenarios_test.clj
```

The namespaces are `hello-world.core` and `easyracer.scenarios` (with tests in `easyracer.scenarios-test`).
