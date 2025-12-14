pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Pas besoin de versionCatalogs ici, Gradle le gère automatiquement.
}

rootProject.name = "Smart_Study"
include(":app")
