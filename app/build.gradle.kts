// SPDX-License-Identifier: Apache-2.0
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
val appVersionCode: Int by rootProject.extra
android {
    namespace = "dev.openautoai.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.openautoai.app"
        minSdk = 29
        targetSdk = 36
        versionCode = appVersionCode
        versionName = project.version.toString()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
dependencies {
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // Android 17 requires the public InputManager/TestLooperManager paths in Espresso 3.7.
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation(project(":settings"))
    implementation(project(":providers"))
    implementation(project(":storage"))
    implementation(project(":voice"))
    implementation(project(":speech-output"))
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.12.0")
}
