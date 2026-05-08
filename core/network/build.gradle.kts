plugins {
    alias(libs.plugins.notes.android.library)
    alias(libs.plugins.notes.android.hilt)
}

android {
    namespace = "com.notes.core.network"
}

dependencies {
    implementation(projects.core.model)

    // Retrofit para chamadas HTTP
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // OkHttp para logging de requests (útil em debug)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
}

