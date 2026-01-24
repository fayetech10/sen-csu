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
    // SUPPRIMEZ le bloc 'versionCatalogs' s'il essaie de charger libs.versions.toml manuellement
}

rootProject.name = "Sencsu"
include(":app")