plugins {
    id("notes.android.library")
    id("notes.android.hilt")
}

android {
    namespace = "com.notes.core.data"
}

dependencies {
    implementation(projects.core.model)
}
