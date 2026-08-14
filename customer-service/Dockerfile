# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
FROM gradle:9.6-jdk21 AS build
WORKDIR /workspace

# Copy both the common and customer-service directories into the container's workspace.
COPY common/ common/
COPY customer-service/ customer-service/

# Switch the working directory to the service and run the build
WORKDIR /workspace/customer-service
RUN gradle bootJar --no-daemon

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /workspace/customer-service/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]