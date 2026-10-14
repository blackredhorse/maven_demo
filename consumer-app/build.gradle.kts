plugins {
    id("com.android.application") version "8.13.2"
}

android {
    namespace = "com.example.consumer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.consumer"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Resolved from the repository published by the sibling build.
    implementation("com.example:mylibrary:1.0.0")
}
