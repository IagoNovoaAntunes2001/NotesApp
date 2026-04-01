import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Plugin: notes.android.hilt
 *
 * Adiciona Hilt a qualquer módulo Android (library ou application).
 * Aplica os plugins hilt + ksp e injeta as dependências automaticamente.
 *
 * Uso: plugins { id("notes.android.hilt") }
 *
 * Substitui estas 4 linhas que todo módulo com Hilt repetia:
 *   alias(libs.plugins.hilt.android)
 *   alias(libs.plugins.ksp)
 *   implementation(libs.hilt.android)
 *   ksp(libs.hilt.compiler)
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            // Acessa o version catalog "libs" para pegar as dependências corretas
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                "implementation"(libs.findLibrary("hilt-android").get())
                "ksp"(libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
