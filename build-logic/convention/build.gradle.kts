import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// kotlin-dsl: habilita escrever plugins Gradle em Kotlin com type-safe DSL.
// É o coração do build-logic — sem ele não conseguimos referenciar
// tipos como LibraryExtension, ApplicationExtension, etc.
plugins {
    `kotlin-dsl`
}

group = "com.notes.buildlogic"

// O build-logic roda na JVM do Gradle, então pode usar Java 17.
// Os módulos Android compilados PELOS plugins continuam em Java 11.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

// JARs dos próprios plugins Gradle — precisamos deles para ESCREVER convenções.
// compileOnly: em runtime o projeto Android já os tem no classpath.
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

// Registra cada plugin com um ID curto que os módulos usarão:
// id("notes.android.library"), id("notes.android.hilt"), etc.
gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "notes.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "notes.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "notes.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidApplication") {
            id = "notes.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "notes.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
    }
}

