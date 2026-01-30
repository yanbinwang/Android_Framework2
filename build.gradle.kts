// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val objectbox = "5.0.1"
    dependencies {
        // 使用列表管理依赖
        val pluginDependencies = listOf(
            "io.objectbox:objectbox-gradle-plugin:$objectbox",
            "com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.ksp}"
        )
        // 遍历列表添加依赖
        pluginDependencies.forEach { classpath(it) }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.therouter.gradle) apply false
}