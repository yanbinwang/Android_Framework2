package com.example.common.config

import java.util.Locale

/**
 * author: wyb
 * date: 2018/9/17.
 * 阿里模块跳转配置
 */
object ARouterPath {
    /**
     * main模块
     */
    const val SplashActivity = "/app/SplashActivity"
    const val StartActivity = "/app/StartActivity"
    const val MainActivity = "/app/MainActivity"
    const val LoginActivity = "/app/LoginActivity"
    const val TestActivity = "/app/TestActivity"

    /**
     * home模块
     */
    const val WebActivity = "/home/WebActivity"
    const val ScaleActivity = "/home/ScaleActivity"

    /**
     * 获取路径
     */
    fun String.simpleName(type: Int = 0): String {
        val group = when (type) {
            0 -> "/app/"
            1 -> "/home/"
            2 -> "/evidence/"
            else -> "/account/"
        }
        return this.replace(group, "").lowercase(Locale.getDefault())
    }
}