# clojure-hello-world

Minimal Clojure “Hello, World!” using [Apache Maven](https://maven.apache.org/) and [vivid `clojure-maven-plugin`](https://github.com/vivid-inc/clojure-maven-plugin), plus a root [`deps.edn`](https://clojure.org/reference/deps_and_cli) for the Clojure CLI.

Sources live under `src/main/clojure/` (Maven-style layout).

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

3. **Clojure CLI** (`clojure` on your `PATH`) is required for the vivid `clojure` goal, which runs `clojure -Scp …` with the Maven classpath. Install it from the [official getting started guide](https://clojure.org/guides/install_clojure).

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

## Layout

```text
├── deps.edn
├── pom.xml
└── src/
    └── main/
        └── clojure/
            └── hello_world/
                └── core.clj
```

The namespace is `hello-world.core` (file path `hello_world/core.clj`).
