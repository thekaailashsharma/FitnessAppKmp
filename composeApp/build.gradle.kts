import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
}

// ── Secrets → generated commonMain source ────────────────────────────────
// Reads keys from the gitignored local.properties (or matching env vars for CI)
// and generates a Kotlin file, so API keys never live in version-controlled source.
// Shared across Android + iOS because it lands in commonMain.
val secretsProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String, default: String = ""): String =
    secretsProperties.getProperty(key) ?: System.getenv(key) ?: default

val generateAppSecrets by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/appSecrets/commonMain/kotlin")
    outputs.dir(outputDir)
    val appleKey = secret("REVENUE_CAT_APPLE_API_KEY")
    val googleKey = secret("REVENUE_CAT_GOOGLE_API_KEY")
    val testKey = secret("REVENUE_CAT_TEST_STORE_API_KEY")
    val geminiKey = secret("GEMINI_API_KEY")
    // Re-run generation whenever any value changes.
    inputs.property("appleKey", appleKey)
    inputs.property("googleKey", googleKey)
    inputs.property("testKey", testKey)
    inputs.property("geminiKey", geminiKey)
    doLast {
        val pkgDir = outputDir.get().asFile.resolve("org/awi/fitness")
        pkgDir.mkdirs()
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
        pkgDir.resolve("RevenueCatSecrets.kt").writeText(
            """
            |// GENERATED — do not edit. Values come from local.properties (gitignored).
            |package org.awi.fitness
            |
            |internal const val REVENUE_CAT_TEST_STORE_API_KEY = "${esc(testKey)}"
            |internal const val REVENUE_CAT_GOOGLE_API_KEY = "${esc(googleKey)}"
            |internal const val REVENUE_CAT_APPLE_API_KEY = "${esc(appleKey)}"
            |internal const val GEMINI_API_KEY = "${esc(geminiKey)}"
            |""".trimMargin() + "\n"
        )
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Required for RevenueCat KMP 3.0.0 iOS bindings
    sourceSets.named { it.lowercase().startsWith("ios") }.configureEach {
        languageSettings {
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    sourceSets {
        commonMain {
            // Pull in the generated RevenueCatSecrets.kt (wires the generate task as a dependency).
            kotlin.srcDir(generateAppSecrets)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.composeIcons.cssGg)
            implementation(libs.composeIcons.weatherIcons)
            implementation(libs.composeIcons.evaIcons)
            implementation(libs.composeIcons.feather)
            implementation(libs.composeIcons.fontAwesome)
            implementation(libs.composeIcons.lineAwesome)
            implementation(libs.composeIcons.linea)
            implementation(libs.composeIcons.octicons)
            implementation(libs.composeIcons.simpleIcons)
            implementation(libs.composeIcons.tablerIcons)

            // Lottie (compottie) — for hand-drawn micro-moments
            implementation("io.github.alexzhirkevich:compottie:2.0.0-rc04")
            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-rc04")

            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.bottomSheetNavigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)

            // Datetime
            implementation(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Settings
            implementation(libs.multiplatform.settings)

            // RevenueCat
            implementation(libs.purchases.kmp.core)

            // RSS Parser (Discover tab)
            implementation("com.prof18.rssparser:rssparser:6.0.6")

            // Haze — real backdrop-blur liquid glass (matches CMP 1.9.3)
            implementation(libs.haze)
            implementation(libs.haze.materials)
        }

        iosMain.dependencies {
            // Empty - only native Firebase initialization here
            implementation(libs.ktor.client.darwin)
        }

        androidMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
            // Firebase Cloud Messaging — retention push token on Android.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation("androidx.browser:browser:1.6.0")
            val media3Version = "1.2.1"
            implementation("androidx.media3:media3-exoplayer:$media3Version")
            implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
            implementation("androidx.media3:media3-ui:$media3Version")
            implementation("androidx.media3:media3-common:$media3Version")
        }
    }
}

android {
    namespace = "org.awi.tajly"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.awi.tajly"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Keep transitive androidx.activity pinned to the project's version so a newer
// transitive (e.g. from androidx.browser) doesn't force AGP 8.9 / compileSdk 36.
configurations.all {
    resolutionStrategy {
        force("androidx.activity:activity:1.10.1")
        force("androidx.activity:activity-ktx:1.10.1")
        force("androidx.activity:activity-compose:1.10.1")
    }
}

dependencies {
    implementation(libs.androidx.material3.android)
    debugImplementation(compose.uiTooling)
}

