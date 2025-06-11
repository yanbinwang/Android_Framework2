// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val greendao = "3.3.1"
    val objectbox = "4.2.0"
    dependencies {
        // 使用列表管理依赖
        val pluginDependencies = listOf(
            "org.greenrobot:greendao-gradle-plugin:$greendao",
            "io.objectbox:objectbox-gradle-plugin:$objectbox"
        )
        // 遍历列表添加依赖
        pluginDependencies.forEach { classpath(it) }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

///**
// * 定义配置 ManifestPlaceholders 的函数
// * manifestPlaceholders 是每个模块独立的配置项。每个模块在构建时会使用自身的 build.gradle.kts 文件中的配置，
// * 包括 manifestPlaceholders。即使模块之间存在依赖关系，被依赖模块的 manifestPlaceholders 配置也不会自动传递给依赖模块
// *
// * 子模块的 build.gradle.kts
// * android {
// *     defaultConfig {
// *         // 调用扩展函数
// *         configureManifestPlaceholders()
// *         //添加字符串资源 --> val googleAuthApi = getResources().getString(R.string.google_auth_api)动态往string里插入一个资源
// *         resValue("string", "google_auth_api", manifestPlaceholders["GOOGLE_AUTH_API"].toString())
// *     }
// * }
// */
//fun configureManifestPlaceholders(defaultConfig: com.android.build.api.dsl.ApplicationDefaultConfig) {
//    defaultConfig.manifestPlaceholders.putAll(
//        mutableMapOf(
//            "PACKAGE_NAME" to libs.versions.applicationId.get(),
//            "DESIGN_WIDTH" to libs.versions.designWidth.get(),
//            "DESIGN_HEIGHT" to libs.versions.designHeight.get(),
//            "AMAP_API_KEY" to libs.versions.amapApiKey.get()
//        )
//    )
//}