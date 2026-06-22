import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

@Suppress("DEPRECATION")
android {
    namespace = "com.example.debugging"

    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()
    }

    buildFeatures {
        dataBinding = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
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
    // 框架库-》额外注意，debug包的时候才会把代码打入
    debugImplementation(project(":lib_thirdparty"))
    // 报错抓取库
    debugImplementation(libs.bundles.debugging)
}