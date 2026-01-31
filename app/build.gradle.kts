import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.android.build.api.dsl.ApplicationExtension

fun getTimestamp(): String {
    val sdf = SimpleDateFormat("yyMMddHH", Locale.US)
    return sdf.format(Date())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val majorVersion = 1
val minorVersion = 0

base {
    archivesName.set("pt-timer-${majorVersion}_${minorVersion}")
}

kotlin {
    jvmToolchain(17)
}

extensions.configure<ApplicationExtension> {
    namespace = "com.example.pt_timer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pt_timer"
        minSdk = 26
        targetSdk = 36

        versionCode = majorVersion * 1000 + minorVersion
        versionName = "$majorVersion.$minorVersion.${getTimestamp()}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("androidx.navigation:navigation-compose:2.9.7")
    //noinspection UseTomlInstead (needed for older Android versions)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
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
