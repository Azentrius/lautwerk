// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    implementation("com.squareup.okhttp3:okhttp:5.5.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.5.0")
    // Android supplies org.json at runtime. The JVM jar is only for compilation and offline tests.
    compileOnly("org.json:json:20260814")
    testImplementation("org.json:json:20260814")
    testImplementation("junit:junit:4.13.2")
}

// Existing fixture assertions use German; localization tests explicitly cover English.
tasks.withType<Test>().configureEach { systemProperty("user.language", "de"); systemProperty("user.country", "DE") }
