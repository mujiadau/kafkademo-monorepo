import org.gradle.kotlin.dsl.kotlin

pluginManagement{
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.10"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("sharedLibs") {
            // Evaluates to: kafkademo-monorepo/gradle-toolchain/lib.versions.toml
            from(files("../gradle-toolchain/gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "customer-service"

if (System.getenv()["IS_CICD"] == null) {
    includeBuild("../gradle-toolchain")
}

includeBuild("../common")