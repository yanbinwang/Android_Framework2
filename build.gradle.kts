// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val firebase = "2.9.5"
    val gms = "4.3.15"
    dependencies {
        // 使用列表管理依赖
        val pluginDependencies = listOf(
            "com.google.firebase:firebase-crashlytics-gradle:$firebase",
            "com.google.gms:google-services:$gms"
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