plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(project(":providers"))
    testImplementation("org.json:json:20240303")
}
