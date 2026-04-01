import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Plugin: notes.android.application.compose
 *
 * Estende notes.android.application adicionando Compose ao :app.
 * Deve ser aplicado JUNTO com notes.android.application.
 *
 * Uso:
 *   plugins {
 *     id("notes.android.application")
 *     id("notes.android.application.compose")
 *   }
 */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}
