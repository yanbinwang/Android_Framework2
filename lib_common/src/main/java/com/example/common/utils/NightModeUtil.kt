package com.example.common.utils

import androidx.appcompat.app.AppCompatDelegate
import com.aliya.uimode.UiModeManager
import com.example.common.BaseApplication.Companion.instance
import com.example.common.config.CacheData.nightMode

/**
 * 黑白页模式操作类
 */
object NightModeUtil {
    val isNightMode get() = nightMode.get()

    fun init() {
        UiModeManager.init(instance, null)
        UiModeManager.setDefaultUiMode(AppCompatDelegate.MODE_NIGHT_NO)
        changeNightMode(isNightMode)
    }

    fun changeNightMode(mode: Boolean) {
        UiModeManager.setUiMode(
            if (mode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        nightMode.set(mode)
        AppManager.forEach { recreate() }
        System.gc()
    }

    fun toggleNightMode() {
        changeNightMode(!isNightMode)
    }

}