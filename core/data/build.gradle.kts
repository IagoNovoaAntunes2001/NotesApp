plugins {
    alias(libs.plugins.notes.android.library)
    alias(libs.plugins.notes.android.hilt)
}

android {
    namespace = "com.notes.core.data"
}

dependencies {
    implementation(projects.core.model)
}
