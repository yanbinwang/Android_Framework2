plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.thirdparty"

    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    api(project(":lib_common"))
    // 文件压缩
    implementation("id.zelory:compressor:3.0.1")
//    //谷歌三方登錄->淘汰
//    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // 谷歌三方登錄
    implementation("androidx.credentials:credentials:1.3.0")
    // 统一且安全的用户凭证管理方式
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    // 支持 Google 登录等联合登录方式
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // 谷歌推送
    implementation(platform("com.google.firebase:firebase-bom:31.3.0"))
    // 动态链接功能
    implementation("com.google.firebase:firebase-dynamic-links-ktx")
    // 保障应用稳定性，自动捕获崩溃
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    // 提供用户行为洞察，优化产品体验
    implementation("com.google.firebase:firebase-analytics-ktx")
    // 实现灵活的消息推送，支持主题广播和单播。
    implementation("com.google.firebase:firebase-messaging-ktx")
}