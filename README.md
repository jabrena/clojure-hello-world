# clojure-hello-world

Minimal Clojure “Hello, World!” plus [Project Euler](https://projecteuler.net/) solutions, using a root [`deps.edn`](https://clojure.org/reference/deps_and_cli) for the Clojure CLI.

- Main sources: `src/main/clojure/` (for example `info.jab.hw`, `info.jab.euler.problem1`)
- Tests and fixtures: `src/test/clojure/`, `src/test/resources/` (for example `euler/answers.txt`)

## Prerequisites (Java 25)

Use **JDK 25** for this project (matches CI and `.sdkmanrc`).

1. **Use SDKMAN** with the checked-in `.sdkmanrc`:

   ```bash
   sdk env install
   sdk env
   java -version
   ```

   This repo pins `java=25.0.2-graalce`, and `java -version` should show that runtime.

2. **Clojure CLI** (`clojure` on your `PATH`) is required. Install the CLI from the [official getting started guide](https://clojure.org/guides/install_clojure).

```bash
brew install clojure/tools/clojure
```

## Run

```bash
clojure -M:run
```

## Tests

The `:test` alias puts `src/test/clojure` and `src/test/resources` on the classpath (so `io/resource` can load `euler/answers.txt`) and runs [cognitect-labs/test-runner](https://github.com/cognitect-labs/test-runner) over `src/test/clojure/info/jab/euler`, i.e. namespaces under `info.jab.euler` whose names end in `-test`.

```bash
clojure -M:test
```

## Docker

```bash
docker compose build verify
docker compose build --no-cache verify
docker compose run --rm verify
docker compose down
```
