plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.kxy.theweather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kxy.theweather"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") {
                storeFile = file("/Users/sixueyang/AndroidStudioProjects/TheWeather/tw.keystore")
                storePassword = "123456"
                keyAlias = "key0"
                keyPassword = "123456"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation( libs.kotlinx.coroutines.android)
    implementation (libs.androidx.work.runtime.ktx)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.adapter.rxjava)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.dialogx)
    implementation(libs.dialogxiosstyle)
    implementation(libs.utilcodex)
    implementation(libs.xxpermissions)
    implementation (libs.mpandroidchart)
}