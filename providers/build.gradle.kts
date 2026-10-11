plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    // Android supplies org.json at runtime. The JVM jar is only for compilation and offline tests.
    compileOnly("org.json:json:20240303")
    testImplementation("org.json:json:20240303")
    testImplementation("junit:junit:4.13.2")
}
