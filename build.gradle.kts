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