plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.common"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    sourceSets {
        getByName("main") {
            res {
                srcDirs(
                    "src/main/res",
                    "src/main/res-public",
                    "src/main/res-loading"
                )
            }
        }
    }

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
    }

    // arouter 编译
    kapt {
        arguments {
            arg("AROUTER_MODULE_NAME", project.name)
        }
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
    androidTestImplementation(libs.bundles.android.testing)
    //框架库
    api(project(":lib_framework"))
    //不依赖于common的库统一放在lib_local里
    api(project(":lib_local:glide"))
    api(project(":lib_local:topsheet"))
    //网络请求
    api(libs.bundles.networking)
    //其余第三方库
    api(libs.autosize)
    api(libs.smartrefresh)
    api(libs.guide)
    api(libs.xxpermissions)
    implementation(libs.stomp)
    implementation(libs.mmkv)
    implementation(libs.immersionbar)
    //页面路由
    implementation(libs.alibaba.arouter.api)
    kapt(libs.alibaba.arouter.compiler)
}