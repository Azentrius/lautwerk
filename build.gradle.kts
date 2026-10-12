// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    id("com.android.application") version "9.4.1" apply false
    id("com.android.library") version "9.4.1" apply false
    id("org.jetbrains.kotlin.jvm") version "2.4.21" apply false
}

// The version lives in gradle.properties (`version`). Both apps use it as versionName and derive
// versionCode from it: X.Y.Z -> X * 10000 + Y * 100 + Z (0.1.16 -> 116), so it always grows.
val appVersionCode by extra(
    version.toString().substringBefore("-").split(".").map(String::toInt).let { (major, minor, patch) ->
        require(minor < 100 && patch < 100) { "versionCode needs minor and patch below 100: $version" }
        major * 10_000 + minor * 100 + patch
    }
)
