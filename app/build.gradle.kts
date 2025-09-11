import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            enableV1Signing = true
            enableV2Signing = true
        }
        create("customDebug") {
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

//    sourceSets {
//        getByName("main") {
//            res {
//                // 添加资源目录
//                srcDirs("src/main/res", "src/main/res-bg", "src/main/res-race")
//                // 过滤资源
//                exclude("**/mipmap-mdpi/**", "**/mipmap-xxxhdpi/**")
//            }
//        }
//    }

    namespace = "com.example.mvvm"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 减少语言支持
        resourceConfigurations.add("zh")
        // dex 突破 65535 的限制
        multiDexEnabled = true
//        // 告知 Gradle 只打包 hdpi、xhdpi 和 xxhdpi 这三种屏幕密度的资源->如果23最低版本，启用这行
//        resConfigs("hdpi", "xhdpi", "xxhdpi")
        // Manifest 配置引用
        manifestPlaceholders.putAll(
            mutableMapOf(
                "PACKAGE_NAME" to libs.versions.applicationId.get(),
                "DESIGN_WIDTH" to libs.versions.designWidth.get(),
                "DESIGN_HEIGHT" to libs.versions.designHeight.get(),
                "GOOGLE_AUTH_API" to libs.versions.googleAuthApi.get()
            )
        )
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

//    kotlin {
//        jvmToolchain(11)
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

//    // AAB 打包配置
//    bundle {
//        // 此配置用于控制是否按语言对资源进行拆分。当 enableSplit 设置为 true 时，Gradle 会根据应用中包含的不同语言资源，生成多个包含不同语言资源的 AAB 包。
//        language {
//            enableSplit = false
//        }
//        // 该配置用于控制是否按屏幕密度对资源进行拆分。当 enableSplit 设置为 true 时，Gradle 会根据应用中包含的不同屏幕密度的图片资源（如 mdpi、hdpi、xhdpi 等），生成多个包含不同屏幕密度资源的 AAB 包。
//        density {
//            enableSplit = false
//        }
//        // 此配置用于控制是否按 CPU 架构对原生库进行拆分。当 enableSplit 设置为 true 时，Gradle 会根据应用中包含的不同 CPU 架构的原生库（如 armeabi-v7a、arm64-v8a、x86 等），生成多个包含不同 CPU 架构原生库的 AAB 包。
//        abi {
//            enableSplit = false
//        }
//    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("customDebug")
        }

        release {
            // 架构支持
            ndk {
                abiFilters.add("arm64-v8a")
            }

            // 剔除冗余资源/定义了资源拆分的配置块，用于对 APK 进行不同维度的拆分，比如按屏幕密度、ABI 架构、语言等（为兼容21，更高版本就可注释掉）
            splits {
                // 专门针对屏幕密度进行资源拆分的配置块
                density {
                    // 启用按屏幕密度进行资源拆分的功能。当设置为 true 时，Gradle 会根据不同的屏幕密度生成多个 APK 文件，每个 APK 只包含对应屏幕密度的资源，这样可以减小 APK 包的大小。
                    isEnable = true
                    // 重置之前可能存在的屏幕密度配置，确保后续的配置是全新的，不会受到之前配置的影响。
                    reset()
                    // 指定需要包含在拆分中的屏幕密度资源。这里指定了 `mipmap-hdpi`、`mipmap-xhdpi` 和 `mipmap-xxhdpi` 这三种屏幕密度的资源会被包含在拆分后的 APK 中，其他屏幕密度的资源则不会被包含。
                    include("mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi")
                    // 定义应用所兼容的屏幕尺寸。这里指定了应用兼容 `small`（小屏幕）、`normal`（正常屏幕）、`large`（大屏幕）和 `xlarge`（超大屏幕）这四种尺寸的设备。在生成的 APK 的 `AndroidManifest.xml` 文件中会添加相应的 `<compatible-screens>` 标签来声明这些兼容性。
                    compatibleScreens("small", "normal", "large", "xlarge")
                }
            }

            // 去除gradle升级版本后映射文件的警告
            lint {
                // 不检查发布版本的构建
                checkReleaseBuilds = false
                // 出现错误时不终止构建
                abortOnError = false
            }

            isMinifyEnabled = true //最小化资源包
            isShrinkResources = true //去掉无用资源
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            android.applicationVariants.all {
                val appName = "example"
                val date = SimpleDateFormat("yyyyMMdd").format(Date())
                outputs.all {
                    if (this is ApkVariantOutputImpl) {
                        outputFileName = "${appName}_v${versionName}_${date}.apk"
                    } else {
                        // 打包命令 ./gradlew bundleRelease -->执行后产生的aab包的路径：项目/app/build/outputs/bundle/release/XXX.aab
                        // AndroidStudio手动打包，先在项目目录下创建outputs/bundle/release对应的文件夹，然后打包路径选择这个，就会输出到目录下
                        val provider = layout.projectDirectory.file("outputs/bundle/release/${appName}_v${versionName}_${date}.aab")
                        // AndroidStudio手动打包，直接给出绝对路径，打包输出至桌面
//                        val file = file("${System.getProperty("user.home")}/Desktop/${appName}_v${versionName}_${date}.aab")
                        val fileProperty = outputFile
                        if (fileProperty is RegularFileProperty) {
                            fileProperty.set(provider)
//                            fileProperty.set(file)
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.testing)
    //调试库
    debugImplementation(project(":lib_debugging"))
    //基础库
    implementation(project(":module_home"))
    implementation(project(":module_account"))
    //页面路由
    kapt(libs.alibaba.arouter.compiler)

    //扫码
    api("com.github.bingoogolapple.BGAQRCode-Android:zxing:1.3.8")
}