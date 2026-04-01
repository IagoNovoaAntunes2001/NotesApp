pluginManagement {
    // includeBuild("build-logic") torna os convention plugins disponíveis
    // para todos os subprojects como se fossem plugins externos.
    // O Gradle compila o build-logic ANTES de avaliar os demais módulos.
    includeBuild("build-logic")
    repositories {
        // Repositório local com os JARs do modules-graph-assert baixados via PowerShell
        // (o JVM não confia no certificado de plugins.gradle.org, mas o Windows sim)
        maven { url = uri(file("gradle/local-plugins")) }
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
