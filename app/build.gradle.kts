plugins {
    alias(libs.plugins.notes.android.application)
    alias(libs.plugins.notes.android.application.compose)
    alias(libs.plugins.notes.android.hilt)
}

android {
    namespace = "com.notes"

    defaultConfig {
        applicationId = "com.notes"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.home)
    implementation(projects.detail)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.designSystem)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling) // só necessário para Previews em debug
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}