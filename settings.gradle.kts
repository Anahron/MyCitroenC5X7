pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven ( url = "https://repository.mulesoft.org/nexus/content/repositories/public/" )
        maven(url = "https://jitpack.io")
        mavenCentral()
    }
}

rootProject.name = "MyCitroenC5X7"
include(":app")
 