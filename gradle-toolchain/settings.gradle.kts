dependencyResolutionManagement {
    versionCatalogs {
        create("sharedLibs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}