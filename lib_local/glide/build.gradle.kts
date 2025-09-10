plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.glide"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.testing)
    // 框架库
    implementation(project(":lib_framework"))
    // 进度条库依赖，可注释
    api("com.dinuscxj:circleprogressbar:1.3.6")
    // 调色盘 依赖
    api("androidx.palette:palette:1.0.0")
    // Glide 依赖
    api("com.github.bumptech.glide:glide:5.0.4")
    kapt("com.github.bumptech.glide:compiler:5.0.4")
    // OkHttp 依赖
    api("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.4")
}