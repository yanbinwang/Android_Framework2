package com.example.mvvm.utils.track

/**
 * 埋点枚举类
 */
enum class TrackEvent(val tag: String) {
    /**
     * 点击Banner的总次数，点击了哪个banner(banner点击数)
     */
    HOME_BANNER("Home_banner"),

    /**
     * 入口点击的次数(头像（左上角）)
     */
    HOME_PORTRAIT("Home_portrait")
}