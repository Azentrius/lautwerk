pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "OpenAutoAI"
include(":app", ":prototype-app", ":core", ":settings", ":car-ui", ":providers", ":storage", ":voice")
