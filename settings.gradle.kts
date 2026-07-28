pluginManagement{
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "kafkademo-monorepo"

include("common")
include("transaction-service")