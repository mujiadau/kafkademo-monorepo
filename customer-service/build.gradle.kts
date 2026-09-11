plugins {
    java
    alias(sharedLibs.plugins.spring.boot)
    alias(sharedLibs.plugins.spring.dependency.management)
}

group = "ch.kafkademo"
version = "1.1.0" //x-release-please-version

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    // Jackson & Spring Boot
    implementation(sharedLibs.jackson.databind)
    implementation(sharedLibs.spring.boot.starter.data.jpa)
    implementation(sharedLibs.spring.boot.starter.kafka)
    implementation(sharedLibs.spring.boot.starter.json)

    // Database
    runtimeOnly(sharedLibs.postgresql)

    // Testing & Testcontainers
    testImplementation(platform(sharedLibs.testcontainers.bom)) // BOMs still need platform()
    testImplementation(sharedLibs.bundles.testing.common) // Includes the 5 common test dependencies
    testImplementation(sharedLibs.testcontainers.kafka)
    testImplementation(sharedLibs.testcontainers.postgresql)

    testRuntimeOnly(sharedLibs.junit.launcher)

}

tasks.withType<Test> {
    useJUnitPlatform()
}
