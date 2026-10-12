// SPDX-License-Identifier: Apache-2.0
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

// The build tooling (Android Gradle Plugin, Kotlin plugin, the device-test platform)
// pulls in libraries with known vulnerabilities. None of them goes into an APK; they
// only run during the build. Lift them to patched versions, in the plugin classpath
// and in every project configuration (the device-test platform resolves per module).
// Drop an entry once the tooling brings that version or a newer one by itself.
fun isOlder(version: String, than: String): Boolean {
    val a = Regex("\\d+").findAll(version).map { it.value.toInt() }.toList()
    val b = Regex("\\d+").findAll(than).map { it.value.toInt() }.toList()
    for (i in 0 until maxOf(a.size, b.size)) {
        val x = a.getOrElse(i) { 0 }
        val y = b.getOrElse(i) { 0 }
        if (x != y) return x < y
    }
    return false
}

fun patchedToolingVersion(group: String, name: String, version: String?): String? {
    if (version.isNullOrEmpty()) return null
    val patched = when {
        group == "org.bouncycastle" && name.endsWith("-jdk18on") -> "1.85"
        group == "com.google.protobuf" && version.startsWith("3.") &&
            name in setOf("protobuf-java", "protobuf-java-util", "protobuf-kotlin") -> "3.25.5"
        group == "org.jdom" && name == "jdom2" -> "2.0.6.1"
        group == "org.bitbucket.b_c" && name == "jose4j" -> "0.9.6"
        group == "org.apache.commons" && name == "commons-lang3" -> "3.18.0" // comes with commons-compress
        else -> return null
    }
    return if (isOlder(version, patched)) patched else null
}

fun ResolutionStrategy.liftBuildTooling() {
    eachDependency {
        val patched = patchedToolingVersion(requested.group, requested.name, requested.version)
            ?: return@eachDependency
        useVersion(patched)
        because("patched version of a build-tooling library, see settings.gradle.kts")
    }
}

gradle.beforeProject {
    buildscript.configurations.configureEach { resolutionStrategy.liftBuildTooling() }
    configurations.configureEach { resolutionStrategy.liftBuildTooling() }
}

rootProject.name = "OpenAutoAI"
include(":app", ":prototype-app", ":core", ":settings", ":car-ui", ":providers", ":storage", ":voice")
