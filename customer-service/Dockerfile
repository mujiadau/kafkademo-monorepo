# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
FROM gradle:9.6-jdk21 AS build
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./

# All modules referenced by settings.gradle.kts must be present so Gradle can
# configure the multi-module build (only this service's bootJar is assembled).
COPY common common
COPY account-service account-service
COPY customer-service customer-service
COPY transaction-service transaction-service

RUN gradle :customer-service:bootJar --no-daemon

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/customer-service/build/libs/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]



