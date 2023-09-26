package com.example.reader.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import com.example.common.BaseApplication
import com.example.framework.utils.function.value.toSafeInt

object ScreenUtil {
    private val mContext by lazy { BaseApplication.instance.applicationContext }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    @JvmStatic
    fun dip2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toSafeInt()
    }

    /**
     * 获取屏幕宽度
     */
    @JvmStatic
    fun getScreenWidth(): Int {
        val dm = mContext.resources.displayMetrics
        return dm.widthPixels
    }

    /**
     * 获取屏幕高度
     */
    @JvmStatic
    fun getScreenHeight(): Int {
        val dm = mContext.resources.displayMetrics
        return dm.heightPixels
    }

    /**
     * 获取系统屏幕亮度
     */
    @JvmStatic
    fun getSystemBrightness(): Int {
        var systemBrightness = 0
        try {
            systemBrightness = Settings.System.getInt(mContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        return systemBrightness
    }

    /**
     * 获取屏幕最大亮度
     */
    @SuppressLint("DiscouragedApi")
    @JvmStatic
    fun getBrightnessMax(): Int {
        val system = Resources.getSystem()
        val resId = system.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android")
        return if (resId != 0) {
            system.getInteger(resId)
        } else 255
    }

    /**
     * 设置窗口亮度
     */
    @JvmStatic
    fun setWindowBrightness(activity: Activity, percent: Float) {
        val window = activity.window
        val lp = window.attributes
        lp.screenBrightness = percent
        window.attributes = lp
    }

}