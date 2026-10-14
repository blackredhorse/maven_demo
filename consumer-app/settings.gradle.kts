pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // The artifact produced by ../ (scripts/publish-local.sh).
        maven {
            name = "LocalDemo"
            url = uri("../build/local-repo")
        }

        // To consume the artifact from GitHub Packages instead, comment the
        // block above and uncomment this one (a token with `read:packages` is
        // required even for public packages).
        //
        // maven {
        //     name = "GitHubPackages"
        //     url = uri("https://maven.pkg.github.com/<owner>/<repo>")
        //     credentials {
        //         username = System.getenv("GITHUB_ACTOR")
        //         password = System.getenv("GITHUB_TOKEN")
        //     }
        // }
    }
}

rootProject.name = "mylibrary-consumer"
