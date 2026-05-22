plugins {
    alias(libs.plugins.notes.android.library)
    alias(libs.plugins.notes.android.hilt) // já aplica KSP internamente
}

android {
    namespace = "com.notes.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.network)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore — preferências do usuário (lastSyncAt, isDarkTheme)
    implementation(libs.datastore.preferences)

    // Testes unitários
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
