plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.githubconsumer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.githubconsumer"
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
    // Pulled from https://maven.pkg.github.com/<owner>/<repo>
    implementation("com.example:mylibrary:1.0.0")
}
