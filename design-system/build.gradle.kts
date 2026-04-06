plugins {
    alias(libs.plugins.notes.android.library)
    alias(libs.plugins.notes.android.library.compose)
}

android {
    namespace = "com.notes.design_system"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // material (Material2 / View-based) removido — app é 100% Compose.
    // Material3 via compose.material3 já cobre tudo que precisamos.

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
}
