import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Plugin: notes.android.library.compose
 *
 * Estende notes.android.library adicionando suporte a Jetpack Compose.
 * Deve ser aplicado JUNTO com notes.android.library, nunca sozinho.
 *
 * Uso:
 *   plugins {
 *     id("notes.android.library")
 *     id("notes.android.library.compose")
 *   }
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // O Compose Compiler é um plugin separado do compilador Kotlin desde Kotlin 2.0
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}
