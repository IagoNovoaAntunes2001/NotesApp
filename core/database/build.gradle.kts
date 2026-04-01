plugins {
    id("notes.android.library")
    id("notes.android.hilt") // já aplica KSP internamente
}

android {
    namespace = "com.notes.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
