# syntax=docker/dockerfile:1

# ---- Stage 1: build (Temurin 21 JDK + Maven) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Resolve dependencies first so this layer is cached until pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compile and package (tests need a live PostgreSQL, so skip them in the image build).
COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && cp target/*.jar app.jar

# ---- Stage 2: runtime (JRE only, non-root) ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl is used by the HEALTHCHECK below; JRE base ships without it.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user, never root.
RUN groupadd --system --gid 1001 spring \
    && useradd --system --uid 1001 --gid spring --no-create-home spring

COPY --from=build --chown=spring:spring /build/app.jar app.jar

USER spring:spring

# Bind the app to 9090 (Spring reads SERVER_PORT); keep EXPOSE and HEALTHCHECK in sync.
ENV SERVER_PORT=9090
EXPOSE 9090

# Fails (non-2xx) whenever the app or its DB dependency is DOWN.
HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD curl -fsS http://localhost:9090/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
