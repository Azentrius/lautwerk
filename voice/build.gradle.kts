plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(project(":providers"))
    testImplementation("org.json:json:20260814")
}

// Existing fixture assertions use German; localization tests explicitly cover English.
tasks.withType<Test>().configureEach { systemProperty("user.language", "de"); systemProperty("user.country", "DE") }
