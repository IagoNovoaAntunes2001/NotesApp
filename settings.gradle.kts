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

rootProject.name = "Notes"
include(":app")
include(":design-system")
include(":core:model")
include(":core:data")
include(":core:database")
include(":home")
include(":detail")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
