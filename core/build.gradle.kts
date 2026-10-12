plugins { id("org.jetbrains.kotlin.jvm") }
kotlin { jvmToolchain(17) }

dependencies { testImplementation("junit:junit:4.13.2") }

// Existing fixture assertions use German; localization tests explicitly cover English.
tasks.withType<Test>().configureEach { systemProperty("user.language", "de"); systemProperty("user.country", "DE") }
