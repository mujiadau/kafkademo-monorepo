// 1. pluginManagement must be the very first block
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.10"
    }
}

// 2. Define the catalog directly here using a relative path
dependencyResolutionManagement {
    versionCatalogs {
        create("sharedLibs") {
            // Evaluates to: kafkademo-monorepo/gradle-toolchain/lib.versions.toml
            from(files("../gradle-toolchain/gradle/libs.versions.toml"))
        }
    }
}

// 3. Apply settings plugins
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 4. Project naming and includes
rootProject.name = "account-service"

// Use relative paths to include other builds
if (System.getenv()["IS_CICD"] == null) {
    includeBuild("../gradle-toolchain")
}

includeBuild("../common")