import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTimestamp(): String {
    val sdf = SimpleDateFormat("yyMMddHH", Locale.US)
    return sdf.format(Date())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.pt_timer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pt_timer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1

        val majorVersion = 0
        val minorVersion = 8
        versionCode = majorVersion * 1000 + minorVersion
        versionName = "$majorVersion.$minorVersion.${getTimestamp()}"
        val archivesBaseName = "pt-timer-${majorVersion}_${minorVersion}"
        setProperty("archivesBaseName", archivesBaseName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization for your release build.
            isMinifyEnabled = true
            // Enables resource shrinking, which removes unused resources.
            isShrinkResources = true

            // Specifies the ProGuard rules files.
            // 'proguard-android-optimize.txt' is a default file from the Android SDK.
            // 'proguard-rules.pro' is a file in your app's root directory for custom rules.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // This will make it possible to install debug and release versions side by side
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17) // Use 17 for modern Android development
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("androidx.navigation:navigation-compose:2.9.6")
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("androidx.documentfile:documentfile:1.1.0")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}