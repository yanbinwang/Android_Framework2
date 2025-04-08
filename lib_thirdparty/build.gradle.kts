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
                    "src/main/res-album",
                    "src/main/res-media",
                    "src/main/res-share",
                    "src/main/res-pay"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
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
    implementation(project(":lib_common"))
    //oss文件传输
    implementation("com.aliyun.dpa:oss-android-sdk:2.9.21")
    //高德三方扩展
    api(project(":lib_local:amap_sdk"))
    //数据库
    api(project(":lib_local:greendao"))
    //数据库2
    api(project(":lib_local:objectbox"))
    //相机/播放器
    api("com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v9.0.0-release-jitpack")
    api("com.otaliastudios:cameraview:2.7.2")
    //相册/裁剪
    api("com.yanzhenjie:album:2.1.3")
    api("com.yanzhenjie:durban:1.0.1")
    //支付宝/微信
    api("com.alipay.sdk:alipaysdk-android:15.8.17@aar")
    api("com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.8.0")
}