rootProject.name = "morphe-patches-template"

pluginManagement {
    repositories {
        // El plugin de patches y el patcher viven en el registry de Morphe;
        // se consulta primero para no depender del portal de Gradle.
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MorpheApp/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("app.morphe.patches") version "2.1.0"
}
