import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.androidarcache"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.androidarcache"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    /*Retrofit*/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    /*Retrofit*/

    implementation("com.google.ar.sceneform:core:1.17.1") {
        exclude("com.android.support")
    }
    implementation("com.google.ar.sceneform:assets:1.17.1") {
        exclude("com.android.support")
    }
    implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1") {
        exclude("com.android.support")
    }
    implementation("com.google.ar:core:1.47.0") {
        exclude("com.android.support")
    }
    implementation ( "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}