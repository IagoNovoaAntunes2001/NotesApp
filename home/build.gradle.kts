plugins {
    alias(libs.plugins.notes.android.library)
    alias(libs.plugins.notes.android.library.compose)
    alias(libs.plugins.notes.android.hilt)
}

android {
    namespace = "com.notes.home"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.designSystem)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}