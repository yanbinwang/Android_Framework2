plugins {
    alias(libs.plugins.android.library)
    id("org.greenrobot.greendao")
}

android {
    namespace = "com.example.greendao"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    greendao {
        schemaVersion = 1 //数据库版本号（可同步app版本号）-写死不做改动！
        daoPackage = "com.example.greendao.dao" //设置DaoMaster、DaoSession、Dao所在的包名
        targetGenDir = file("src/main/java") //设置DaoMaster、DaoSession、Dao所在的目录
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
    implementation(project(":lib_framework"))
    //数据库
    api("org.greenrobot:greendao:3.3.0")
}