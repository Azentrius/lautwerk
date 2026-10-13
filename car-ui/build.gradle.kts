// SPDX-License-Identifier: Apache-2.0
plugins {
    id("com.android.library")
}
android {
    namespace = "dev.openautoai.car"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":settings"))
    implementation(project(":core"))
    implementation(project(":voice"))
    implementation(project(":providers"))
    implementation(project(":storage"))
    api("androidx.car.app:app:1.9.0-alpha03")
    implementation("androidx.car.app:app-projected:1.9.0-alpha03")
}
