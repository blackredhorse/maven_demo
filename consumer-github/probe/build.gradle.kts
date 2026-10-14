import java.util.zip.ZipFile

plugins {
    `java-library`
}

// A dependency that is resolved *only*, never compiled against. Lets us check
// that GitHub Packages really hands back the AAR/JAR before involving Android.
val published by configurations.creating

val libraryVersion = providers.gradleProperty("library.version").getOrElse("1.0.0")
val coordinates = "com.example:mylibrary:$libraryVersion"

dependencies {
    published("$coordinates@aar")
    published("$coordinates@jar")
    published("$coordinates:sources@jar")
}

tasks.register("verifyArtifact") {
    group = "verification"
    description = "Downloads the published artifacts from GitHub Packages and inspects them."

    val resolved = published
    val expectedCoordinates = coordinates

    doLast {
        val files = resolved.resolve()
        check(files.isNotEmpty()) { "nothing resolved for $expectedCoordinates" }

        files.sortedBy { it.name }.forEach { file ->
            println()
            println("==> ${file.name}  (${file.length()} bytes)")
            println("    ${file.absolutePath}")
            if (file.extension == "aar" || file.extension == "jar") {
                ZipFile(file).use { zip ->
                    zip.entries().asSequence()
                        .filterNot { entry -> entry.isDirectory }
                        .sortedBy { entry -> entry.name }
                        .take(15)
                        .forEach { entry -> println("       ${entry.name}") }
                }
            }
        }

        val aar = files.first { it.extension == "aar" }
        ZipFile(aar).use { zip ->
            val classesJar = zip.getEntry("classes.jar")
                ?: error("AAR has no classes.jar")
            val tmp = File(temporaryDir, "classes.jar")
            zip.getInputStream(classesJar).use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
            ZipFile(tmp).use { inner ->
                check(inner.getEntry("com/example/mylibrary/Greeter.class") != null) {
                    "com/example/mylibrary/Greeter.class is missing from the AAR"
                }
            }
        }

        println()
        println("OK - GitHub Packages served $expectedCoordinates (AAR + JAR + sources).")
    }
}
