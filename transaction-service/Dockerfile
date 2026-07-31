# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
# Build the executable Spring Boot jar. The build context is the repository root
# so the multi-module project can be assembled. Using the official Gradle image
# means the Gradle distribution does not have to be downloaded during the build.
FROM gradle:9.6-jdk21 AS build
WORKDIR /workspace

# Copy the build scripts first for better layer caching.
COPY settings.gradle.kts build.gradle.kts ./

# All modules referenced by settings.gradle.kts must be present so Gradle can
# configure the multi-module build (only this service's bootJar is assembled).
COPY common common
COPY account-service account-service
COPY customer-service customer-service
COPY transaction-service transaction-service

RUN gradle :transaction-service:bootJar --no-daemon

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/transaction-service/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]