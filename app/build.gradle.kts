plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.sencsu"
    compileSdk = 34  // ✅ CORRIGÉ: 34 (Android 14), pas 36

    defaultConfig {
        applicationId = "com.example.sencsu"
        minSdk = 24
        targetSdk = 34  // ✅ CORRIGÉ: 34, pas 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE*"
        }
    }
}

dependencies {
    // ===== Core =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)

    // ===== Activity / Lifecycle =====
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ===== Compose =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.vision.internal.vkp)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // ===== Navigation =====
    implementation(libs.androidx.navigation.compose)

    // ===== Hilt =====
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ===== Coil =====
    implementation(libs.coil.compose)

    // ===== Retrofit / OkHttp =====
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp3.logging.interceptor)

    // ===== DataStore =====
    implementation(libs.androidx.datastore.preferences)

    // ===== Room ===== (si vous l'utilisez)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // ===== Tests =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}
