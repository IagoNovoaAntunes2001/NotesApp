// O build-logic é um "included build" — um projeto Gradle independente
// que gera plugins usados pelo projeto principal.
// Aqui configuramos ONDE ele busca dependências e como acessa o version catalog.

// pluginManagement para o build-logic em si.
// plugins.gradle.org tem SSL inválido no JBR do Android Studio.
// Solução: kotlin-dsl plugin (org.gradle.kotlin:gradle-kotlin-dsl-plugins:5.2.0)
// foi baixado via PowerShell e está no local-plugins maven repo.
// Deps transitivas (kotlin-gradle-plugin etc.) vêm do mavenCentral() que o JBR confia.
pluginManagement {
    repositories {
        maven { url = uri(file("../gradle/local-plugins")) }
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // Reusa o mesmo libs.versions.toml do projeto principal
    // Assim os plugins conhecem as mesmas versões que os módulos Android usam
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
