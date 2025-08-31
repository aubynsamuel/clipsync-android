import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.aubynsamuel.clipsync"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aubynsamuel.clipsync"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildTypes {
        release {
            val boolean = true
            isMinifyEnabled = boolean
            isShrinkResources = boolean
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
            freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    testImplementation(libs.androidx.ui.test.junit4.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)

    // Work manager
    implementation(libs.androidx.work.runtime.ktx)
    // Material Icons
    implementation(libs.androidx.material.icons.extended)
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    // For AppWidgets support
    implementation(libs.androidx.glance.appwidget)
    // For interop APIs with Material 3
    implementation(libs.androidx.glance.material3)
    // For interop APIs with Material 2
    implementation(libs.androidx.glance.material)
    // Material
    implementation(libs.material)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.profileinstaller)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    "baselineProfile"(project(":baselineprofile"))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}