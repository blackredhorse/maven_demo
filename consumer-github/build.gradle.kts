plugins {
    id("com.android.application") version "8.13.2" apply false
}

subprojects {
    tasks.withType<Copy>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
