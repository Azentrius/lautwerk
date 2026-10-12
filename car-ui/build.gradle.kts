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
    api("androidx.car.app:app:1.8.0-rc01")
    implementation("androidx.car.app:app-projected:1.8.0-rc01")
}
