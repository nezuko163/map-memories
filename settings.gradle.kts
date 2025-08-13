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
        mavenCentral()
    }
}

rootProject.name = "map-memories"
include(":app")
include(":feature")
include(":core")
include(":feature:main")
include(":core:data")
include(":core:ui")
include(":core:common")
include(":core:domain")
include(":feature:map")
include(":feature:profile")
include(":feature:search")
include(":feature:memoryDetails")
