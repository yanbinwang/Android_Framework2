import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

@Suppress("DEPRECATION")
android {
    namespace = "com.example.gsyvideoplayer"

    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInt())
    }

    sourceSets {
        getByName("main") {
            res {
                srcDirs(
                    "src/main/res",
                    "src/main/res-custom"
                )
            }
        }
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
    // 框架库
    implementation(project(":lib_common"))
    // 播放器
    api("com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v11.1.0")
}