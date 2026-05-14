# Linux image: runs the hello-world entrypoint.
# Build:  docker compose build verify
# Run:    docker compose run --rm verify

FROM clojure:temurin-21-tools-deps

WORKDIR /app

# Prime dependency cache first for better layer reuse.
COPY deps.edn ./
RUN clojure -P

# Copy project sources after dependency resolution.
COPY src ./src
COPY README.md ./

CMD ["clojure", "-M:run"]
