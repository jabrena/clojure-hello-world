# clojure-hello-world

Minimal Clojure “Hello, World!” with a root [`deps.edn`](https://clojure.org/reference/deps_and_cli) for the Clojure CLI.

Sources live under `src/main/clojure/`.

## Prerequisites (Java 21)

Use **JDK 21** for this project.

1. **Use SDKMAN** with the checked-in `.sdkmanrc`:

   ```bash
   sdk env install
   sdk env
   java -version
   ```

   This repo pins `java=21.0.2-graalce`, and `java -version` should show a Java 21 runtime.

2. **Clojure CLI** (`clojure` on your `PATH`) is required. Install the CLI from the [official getting started guide](https://clojure.org/guides/install_clojure).

```bash
brew install clojure/tools/clojure
```

## Run

```bash
clojure -M:run
```

## Docker

```bash
docker compose build verify
docker compose build --no-cache verify
docker compose run --rm verify
docker compose down
```
