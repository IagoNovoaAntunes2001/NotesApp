plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.notes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.notes"
        minSdk = 24
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Features — :app só importa os navGraphs, não conhece internals de cada feature
    implementation(projects.home)
    implementation(projects.detail)

    // :core:data — necessário para carregar o dataModule (UseCases) no startKoin
    // as features dependem dele também, mas :app precisa instanciá-lo no DI
    implementation(projects.core.data)

    // :core:database — necessário para carregar o databaseModule no startKoin
    // é aqui que a implementação do TopicRepository ganha vida (Room + DAO)
    // as features NUNCA dependem disto — elas só conhecem a interface em :core:data
    implementation(projects.core.database)

    // Design System
    implementation(projects.designSystem)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}