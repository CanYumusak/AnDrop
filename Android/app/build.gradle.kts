plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    buildFeatures {
        viewBinding = true
    }

    compileSdk = 31
    defaultConfig {
        applicationId = "de.canyumusak.androiddrop"
        minSdk = 24
        targetSdk = 31
        versionCode = 150
        versionName = "1.5"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }

    kotlinOptions {
        jvmTarget = "15"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.core:core-ktx:1.7.0")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

    implementation("com.android.billingclient:billing:4.0.0")
}