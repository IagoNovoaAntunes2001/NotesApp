plugins {
    id("notes.android.library")
    id("notes.android.library.compose")
}

android {
    namespace = "com.notes.design_system"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
}
