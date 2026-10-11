plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.openautoai.prototype"
    compileSdk = 36
    defaultConfig {
        applicationId = "dev.openautoai.prototype"
        minSdk = 28
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.2-prototype"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "surface"
    productFlavors {
        create("phone") {
            dimension = "surface"
            applicationIdSuffix = ".phone"
            buildConfigField("boolean", "DHU_EXPERIMENT", "false")
        }
        create("dhu") {
            dimension = "surface"
            applicationIdSuffix = ".dhu"
            buildConfigField("boolean", "DHU_EXPERIMENT", "true")
        }
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }

// A diagnostic category placeholder must never become a distributable release.
androidComponents {
    beforeVariants(selector().withBuildType("release").withFlavor("surface" to "dhu")) {
        it.enable = false
    }
}

dependencies {
    implementation(project(":settings"))
    implementation(project(":core"))
    "dhuImplementation"(project(":car-ui"))
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    "androidTestDhuImplementation"("androidx.car.app:app-testing:1.8.0-rc01") {
        // Device tests must not load Robolectric service providers.
        exclude(group = "org.robolectric")
    }
}
