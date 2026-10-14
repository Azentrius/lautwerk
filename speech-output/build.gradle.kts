// SPDX-License-Identifier: Apache-2.0
plugins { id("com.android.library") }
android {
    namespace = "dev.openautoai.speech"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    api(project(":voice"))
}
