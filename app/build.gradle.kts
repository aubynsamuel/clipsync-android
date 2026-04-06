import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.serialization)
}

val mockitoAgent = configurations.create("mockitoAgent")

android {
    namespace = "com.aubynsamuel.clipsync"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.aubynsamuel.clipsync"
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "1.3.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties["STORE_FILE"] as String)
            storePassword = localProperties["STORE_PASSWORD"] as String
            keyAlias = localProperties["KEY_ALIAS"] as String
            keyPassword = localProperties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            val boolean = true
            isMinifyEnabled = boolean
            isShrinkResources = boolean
            signingConfig = signingConfigs.getByName("release")
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
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Testing
    testImplementation(libs.androidx.ui.test.junit4.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    mockitoAgent(libs.mockito.core) { isTransitive = false }
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
    // Kotlinx serialization
    implementation(libs.kotlinx.serialization.json)

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

tasks.withType<Test>().configureEach {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}