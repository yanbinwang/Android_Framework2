plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.thirdparty"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    sourceSets {
        getByName("main") {
            res {
                srcDirs(
                    "src/main/res",
                    "src/main/res-amap",
                    "src/main/res-media",
                    "src/main/res-pay",
                    "src/main/res-share"
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
    // oss文件传输
    implementation("com.aliyun.dpa:oss-android-sdk:2.9.21")
    // 框架库
    api(project(":lib_common"))
    // 高德三方扩展
    api(project(":lib_local:amap_sdk"))
    // 数据库
    api(project(":lib_local:greendao"))
    // 数据库2
    api(project(":lib_local:objectbox"))
    // 相册
    api(project(":lib_local:gallery"))
    // 相机/播放器
    api("com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:11.1.0")
    api("com.otaliastudios:cameraview:2.7.2")
    // 支付宝/微信
    api("com.alipay.sdk:alipaysdk-android:15.8.38@aar")
    api("com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.8.0")
}