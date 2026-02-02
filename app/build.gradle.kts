import com.android.testing.utils.is16kPageSource
import com.android.utils.FileUtils.copyFile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
//    alias(libs.plugins.therouter.android)
    alias(libs.plugins.devtools.ksp)
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

    namespace = "com.example.mvvm"

    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInt())
    }

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()
        // 减少语言支持
        resourceConfigurations.add("zh")
        // dex 突破 65535 的限制
        multiDexEnabled = true
        // Manifest 配置引用
        manifestPlaceholders.putAll(mutableMapOf(
            "PACKAGE_NAME" to libs.versions.applicationId.get(),
            "DESIGN_WIDTH" to libs.versions.designWidth.get(),
            "DESIGN_HEIGHT" to libs.versions.designHeight.get(),
            "AMAP_API_KEY" to libs.versions.amapApiKey.get()
        ))
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

    // 对所有 jniLibs 下的 SO 库生效，16KB 对齐
    packaging {
        jniLibs {
            is16kPageSource(libs.versions.applicationId.get())
        }
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
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("customDebug")
        }

        release {
            // 去除gradle升级版本后映射文件的警告
            lint {
                // 不检查发布版本的构建
                checkReleaseBuilds = false
                // 出现错误时不终止构建
                abortOnError = false
            }

            // 架构支持
            ndk {
                abiFilters.add("arm64-v8a")
//                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }

//            // 剔除冗余资源/定义了资源拆分的配置块，用于对 APK 进行不同维度的拆分，比如按屏幕密度、ABI 架构、语言等
//            splits {
//                // 专门针对屏幕密度进行资源拆分的配置块
//                density {
//                    // 启用按屏幕密度进行资源拆分的功能。当设置为 true 时，Gradle 会根据不同的屏幕密度生成多个 APK 文件，每个 APK 只包含对应屏幕密度的资源，这样可以减小 APK 包的大小。
//                    isEnable = true
//                    // 重置之前可能存在的屏幕密度配置，确保后续的配置是全新的，不会受到之前配置的影响。
//                    reset()
//                    // 指定需要包含在拆分中的屏幕密度资源。这里指定了 `mipmap-hdpi`、`mipmap-xhdpi` 和 `mipmap-xxhdpi` 这三种屏幕密度的资源会被包含在拆分后的 APK 中，其他屏幕密度的资源则不会被包含。
//                    include("mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi")
//                    // 定义应用所兼容的屏幕尺寸。这里指定了应用兼容 `small`（小屏幕）、`normal`（正常屏幕）、`large`（大屏幕）和 `xlarge`（超大屏幕）这四种尺寸的设备。在生成的 APK 的 `AndroidManifest.xml` 文件中会添加相应的 `<compatible-screens>` 标签来声明这些兼容性。
//                    compatibleScreens("small", "normal", "large", "xlarge")
//                }
//            }

            // 最小化资源包
            isMinifyEnabled = true
            // 去掉无用资源
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

//    // 打包重命名
//    android.applicationVariants.all {
//        val appName = "example"
//        val date = SimpleDateFormat("yyyyMMdd").format(Date())
//        outputs.all {
//            if (this is ApkVariantOutputImpl) {
//                outputFileName = "${appName}_v${versionName}_${date}.apk"
//            } else {
//                // 打包命令 ./gradlew bundleRelease -->执行后产生的aab包的路径：项目/app/build/outputs/bundle/release/XXX.aab
//                // AndroidStudio手动打包，先在项目目录下创建outputs/bundle/release对应的文件夹，然后打包路径选择这个，就会输出到目录下
//                val provider = layout.projectDirectory.file("outputs/bundle/release/${appName}_v${versionName}_${date}.aab")
//                // AndroidStudio手动打包，直接给出绝对路径，打包输出至桌面
////                val file = file("${System.getProperty("user.home")}/Desktop/${appName}_v${versionName}_${date}.aab")
//                val fileProperty = outputFile
//                if (fileProperty is RegularFileProperty) {
//                    fileProperty.set(provider)
////                    fileProperty.set(file)
//                }
//            }
//        }
//    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.testing)
    // 调试库
    debugImplementation(project(":lib_debugging"))
    // 基础库
    implementation(project(":module_home"))
    implementation(project(":module_account"))
    // 页面路由
    ksp(libs.therouter.apt)
}

androidComponents {
    onVariants(selector().all()) { variant ->
        // 提取变体的核心信息（buildType、name），后续用于命名和路径拼接
        val variantName = variant.name // 如 "debug"、"release"
        val variantBuildType = variant.buildType ?: "unknown"
        // 正式包重命名
        if (variantBuildType.equals("release", ignoreCase = true)) {
            // 等待 android {} 所有配置完全生效（确保能获取到正确的 versionName 等）
            project.afterEvaluate {
                // 双重校验，防止偶发漏判（比如variantBuildType为空）
                if (!variantBuildType.equals("release", ignoreCase = true)) return@afterEvaluate
                // 定义命名规则
                val appName = "example"
                val versionName = android.defaultConfig.versionName ?: "1.0.0"
                val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val baseFileName = "${appName}_v${versionName}_${variantBuildType}_${currentDate}"
                val variantNameUppercase = variantName.replaceFirstChar { it.uppercase() }
                // 处理APK：任务名前缀package，产物后缀.apk
                handleBuildArtifact("package", ".apk", variantNameUppercase, baseFileName)
                // 处理AAB：任务名前缀bundle，产物后缀.aab
                handleBuildArtifact("bundle", ".aab", variantNameUppercase, baseFileName)
            }
        }
    }
}

/**
 * 通用产物处理方法
 * @taskPrefix 任务名前缀：package(APK)/bundle(AAB)
 * @artifactSuffix 产物后缀：.apk/.aab
 * @variantNameUppercase 大写变体名：Release/Debug
 * @baseFileName 基础自定义文件名（无后缀）
 */
private fun handleBuildArtifact(taskPrefix: String, artifactSuffix: String, variantNameUppercase: String, baseFileName: String) {
    // 拼接任务名：APK是packageRelease/packageReleaseApk，AAB是bundleRelease
    val taskNames = if (taskPrefix == "package") {
        // APK保留原有的两个候选任务名，兼容AGP9.0的任务名差异
        listOf("${taskPrefix}${variantNameUppercase}Apk", "${taskPrefix}${variantNameUppercase}")
    } else {
        // AAB只有一个固定任务名，直接单元素列表
        listOf("${taskPrefix}${variantNameUppercase}")
    }
    // 遍历任务名，执行原有后置重命名逻辑
    taskNames.forEach { taskName ->
        project.tasks.findByName(taskName)?.doLast {
            // 查找产物文件，复制重命名
            val originalArtifact = findArtifactFromTask(this, artifactSuffix)
            originalArtifact?.let { originalFile ->
                copyAndCleanArtifact(originalFile, "${baseFileName}${artifactSuffix}")
            }
        }
    }
}

/**
 * 从Gradle任务中查找目标产物（APK/AAB），消除重复的任务输出获取逻辑
 * @param task Gradle打包任务（APK/AAB打包任务）
 * @param artifactSuffix 产物后缀（.apk / .aab）
 * @return 找到的有效产物文件，未找到返回null
 */
private fun findArtifactFromTask(task: Task, artifactSuffix: String): File? {
    // 统一获取任务输出文件集合，再调用通用产物查找方法
    val taskOutputFiles = task.outputs.files.files
    return extractOriginalArtifactFromTaskOutput(taskOutputFiles, artifactSuffix)
}

/**
 * 产物复制 + 仅Release版本删除原始文件（消除重复的runCatching样板代码）
 * @param originalFile 原始产物文件（APK/AAB）
 * @param customFileName 自定义产物文件名（含后缀）
 */
private fun copyAndCleanArtifact(originalFile: File, customFileName: String) {
    // 构建自定义文件对象
    val customFile = File(originalFile.parentFile, customFileName)
    // 统一的复制+条件删除逻辑
    runCatching {
        copyFile(originalFile, customFile)
    }.onSuccess {
        // 仅Release版本删除原始文件，Debug版本保留
        if (originalFile.exists()) {
            val deleteSuccess = originalFile.delete()
            // 添加日志，便于排查问题
            val artifactType = if (customFileName.endsWith(".apk")) "APK" else "AAB"
            if (deleteSuccess) {
                println("【原始${artifactType}删除成功】已删除：${originalFile.name}")
            }
        }
    }.onFailure {
        // 统一异常处理
        val artifactType = if (customFileName.endsWith(".apk")) "APK" else "AAB"
        println("【${artifactType}处理失败】异常：${it.message}")
    }
}

/**
 * 通用化产物查找（支持APK/AAB，直接复用flatMapRecursive扩展函数）
 * @artifactSuffix: 传入 ".apk" 或 ".aab"
 */
private fun extractOriginalArtifactFromTaskOutput(taskOutputFiles: Set<File>, artifactSuffix: String): File? {
    // 标记是否为APK格式
    val isApkFile = artifactSuffix == ".apk"
    // 提取APK专属的额外筛选条件
    val apkExtraFilterRules = { file: File ->
        !file.name.contains("unaligned") && !file.name.contains("temp") && !file.name.contains("info")
    }
    return taskOutputFiles.flatMapRecursive { it.listFiles().orEmpty().toList() }.firstOrNull { file ->
        // 区分APK/AAB的筛选规则
        file.isFile && file.name.endsWith(artifactSuffix) && (if (isApkFile) apkExtraFilterRules(file) else true)
    }
}

private fun <T> Collection<T>.flatMapRecursive(transform: (T) -> Collection<T>): List<T> {
    return this + this.flatMap { transform(it).flatMapRecursive(transform) }
}