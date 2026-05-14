# Linux image: runs `info.jab.hw` via `clojure -M:run`.
# Build:  docker compose build verify
# Run:    docker compose run --rm verify

FROM clojure:temurin-21-tools-deps

WORKDIR /app

# Prime dependency cache first for better layer reuse.
COPY deps.edn ./
RUN clojure -P

# Copy project sources after dependency resolution.
COPY src ./src

CMD ["clojure", "-M:run"]
