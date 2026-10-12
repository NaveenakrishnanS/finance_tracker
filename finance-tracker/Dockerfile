# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom first for layer caching
COPY pom.xml .
COPY config/ config/

# Download dependencies in a separate layer (cached unless pom.xml changes)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -q

# Copy source and build, skipping tests (tests run in CI, not in image build)
COPY src/ src/
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/finance-tracker-*.jar app.jar

# Static frontend files are embedded inside the JAR (src/main/resources/static)
# No separate copy needed — Spring Boot serves them automatically.

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

# Use the prod profile; all secrets come from environment variables
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
