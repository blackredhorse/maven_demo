plugins {
    id("com.android.library")
    id("maven-publish")
}

// ---------------------------------------------------------------------------
// Coordinates: com.example:mylibrary:1.0.0
// ---------------------------------------------------------------------------
group = providers.gradleProperty("library.group").getOrElse("com.example")
version = providers.gradleProperty("library.version").getOrElse("1.0.0")
val artifactIdValue = providers.gradleProperty("library.artifactId").getOrElse(project.name)

// ---------------------------------------------------------------------------
// GitHub Packages target: https://maven.pkg.github.com/<owner>/<repo>
// ---------------------------------------------------------------------------
val githubOwner = providers.gradleProperty("github.owner").getOrElse("")
val githubRepo = providers.gradleProperty("github.repo").getOrElse("")
val githubActor = providers.gradleProperty("github.actor")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
    .getOrElse("")
val githubToken = providers.gradleProperty("github.token")
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
    .getOrElse("")

android {
    namespace = "com.example.mylibrary"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// ---------------------------------------------------------------------------
// Plain JAR built from the compiled release classes.
// AGP produces the classes as `bundleLibRuntimeToJarRelease`; repackage them so
// the same coordinates also expose a Maven-friendly `-1.0.0.jar`.
// ---------------------------------------------------------------------------
val androidJar = tasks.register<Jar>("androidJar") {
    description = "Packages the release classes of the Android library into a plain JAR."
    group = "build"

    val releaseClassesJar = tasks.named("bundleLibRuntimeToJarRelease")
    dependsOn(releaseClassesJar)
    from(zipTree(releaseClassesJar.get().outputs.files.singleFile))

    archiveBaseName.set(artifactIdValue)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
    destinationDirectory.set(layout.buildDirectory.dir("outputs/androidJar"))
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = artifactIdValue
            version = project.version.toString()

            // The AAR + its sources jar provided by the Android Gradle Plugin.
            afterEvaluate {
                from(components["release"])
            }
            // The plain JAR produced above. Both files share the same
            // coordinates, only the extension differs (.aar / .jar).
            artifact(androidJar)

            pom {
                name.set(artifactIdValue)
                description.set("Demo Android library published to GitHub Packages as AAR + JAR.")
                url.set("https://github.com/$githubOwner/$githubRepo")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/$githubOwner/$githubRepo.git")
                    developerConnection.set("scm:git:ssh://git@github.com/$githubOwner/$githubRepo.git")
                    url.set("https://github.com/$githubOwner/$githubRepo")
                }

                // Publishing a second artifact with a different extension makes
                // Gradle fall back to <packaging>pom</packaging>. Restore the
                // packaging Maven consumers need to resolve the AAR.
                withXml {
                    val root = asNode()
                    @Suppress("UNCHECKED_CAST")
                    val packaging = root.get("packaging") as List<groovy.util.Node>
                    if (packaging.isEmpty()) {
                        root.appendNode("packaging", "aar")
                    } else {
                        packaging.first().setValue("aar")
                    }
                }
            }
        }
    }

    repositories {
        // 1) A local folder repo. Always available, no credentials, used by
        //    scripts/verify-e2e.sh to prove that the pipeline works end to end.
        maven {
            name = "localRepo"
            url = uri(rootProject.projectDir.resolve("build/local-repo"))
        }

        // 2) GitHub Packages. Only registered when owner/repo are configured,
        //    so an offline build never breaks.
        if (githubOwner.isNotBlank() && githubRepo.isNotBlank()) {
            maven {
                name = "GitHub"
                url = uri("https://maven.pkg.github.com/$githubOwner/$githubRepo")
                credentials {
                    username = githubActor
                    password = githubToken
                }
            }
        }
    }
}

// Fail fast with an actionable message instead of a bare HTTP 401.
tasks.matching { it.name.endsWith("ToGitHubRepository") }.configureEach {
    doFirst {
        check(githubActor.isNotBlank() && githubToken.isNotBlank()) {
            """
            |Missing GitHub credentials.
            |Export GITHUB_ACTOR and GITHUB_TOKEN (PAT with the `write:packages` scope),
            |or put github.actor / github.token into ~/.gradle/gradle.properties.
            """.trimMargin()
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
