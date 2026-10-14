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

val githubOwner = providers.gradleProperty("github.owner").getOrElse("blackredhorse")
val githubRepo = providers.gradleProperty("github.repo").getOrElse("maven_demo")
val githubActor = providers.gradleProperty("github.actor")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
    .getOrElse("")
val githubToken = providers.gradleProperty("github.token")
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
    .getOrElse("")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // The real thing: the artifact published by .github/workflows/publish.yml
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubOwner/$githubRepo")
            credentials {
                username = githubActor
                password = githubToken
            }
        }
    }
}

rootProject.name = "mylibrary-github-consumer"
include(":app")
include(":probe")
