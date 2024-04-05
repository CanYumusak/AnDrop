@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileSdk = 34
    defaultConfig {
        applicationId = "de.canyumusak.androiddrop"
        minSdk = 26
        targetSdk = 33
        versionCode = 210
        versionName = "2.1.0"
    }

    signingConfigs {
        register("release") {

            val keystorePropertiesFile = file("../keystore.properties")

            if (!keystorePropertiesFile.exists()) {
                logger.warn("Release builds may not work: signing config not found.")
                return@register
            }

            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "de.canyumusak.androiddrop"
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))


    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("app.rive:rive-android:4.0.0")
    implementation("androidx.startup:startup-runtime:1.1.1")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.core:core-ktx:1.12.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    implementation("com.android.billingclient:billing:6.2.0")

    implementation("com.google.firebase:firebase-analytics-ktx")
}
