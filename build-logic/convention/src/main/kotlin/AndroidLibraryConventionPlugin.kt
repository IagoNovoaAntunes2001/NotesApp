import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Plugin: notes.android.library
 *
 * Aplica a configuração padrão de todos os módulos Android library.
 * Elimina a repetição de compileSdk, minSdk, compileOptions e kotlinOptions
 * que antes existia em CADA build.gradle.kts.
 *
 * Uso: plugins { id("notes.android.library") }
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 24
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }

            // Garante que todo código Kotlin compilado para Android usa JVM 11
            tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "11"
                }
            }
        }
    }
}

