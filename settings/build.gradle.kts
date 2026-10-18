// SPDX-License-Identifier: Apache-2.0
plugins {
    id("com.android.library")
}
android {
    namespace = "com.azentrius.lautwerk.settings"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
dependencies { api(project(":core")) }
