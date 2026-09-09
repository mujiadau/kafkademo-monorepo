import org.gradle.kotlin.dsl.java

plugins {
    java
    alias(sharedLibs.plugins.spring.boot)
    alias(sharedLibs.plugins.spring.dependency.management)
}

group = "ch.kafkademo"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Internal Modules
    implementation(project(":common"))

    // Jackson & Spring Boot
    implementation(sharedLibs.jackson.databind)
    implementation(sharedLibs.spring.boot.starter.web)
    implementation(sharedLibs.spring.boot.starter.validation)
    implementation(sharedLibs.spring.boot.starter.kafka)
    implementation(sharedLibs.spring.boot.starter.actuator)

    // Spring Security as an OAuth 2.0 / OpenID Connect resource server validating JWT Bearer tokens
    implementation(sharedLibs.spring.boot.starter.security)
    implementation(sharedLibs.spring.boot.starter.oauth2.resource.server)

    // Testing & Testcontainers
    testImplementation(platform(sharedLibs.testcontainers.bom))
    testImplementation(sharedLibs.bundles.testing.common) // Replaces individual core test dependencies
    testImplementation(sharedLibs.spring.security.test)
    testImplementation(sharedLibs.testcontainers.kafka)

    testRuntimeOnly(sharedLibs.junit.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}