package com.example.common.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.common.BaseApplication
import com.example.common.utils.function.getManifestString
import com.example.framework.utils.function.value.min
import com.example.framework.utils.function.value.toSafeInt
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @description 屏幕数值相关类
 * @author yan
 */
object ScreenUtil {

    /**
     * 获取屏幕高度（px）
     * 不会随着各种情况的变化而更新
     */
    val screenHeight by lazy(NONE) { screenHeight() }

    /**
     * 获取屏幕高度（px）
     * 不会随着各种情况的变化而更新
     */
    val screenWidth by lazy(NONE) { screenWidth() }

    /**
     * 获取屏幕比值（px）
     * 不会随着各种情况的变化而更新
     */
    val screenDensity by lazy(NONE) { screenDensity() }

    /**
     * 根据autosize设置来获取设定的宽度
     */
    private val designWidth by lazy { getManifestString("design_width_in_dp").toSafeInt(375) }

    /**
     * 获取屏幕宽度（px）
     */
    private fun screenWidth(context: Context = BaseApplication.instance): Int {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            context.resources.displayMetrics.widthPixels
        } else {
            context.resources.displayMetrics.heightPixels
        }
    }

    /**
     * 获取屏幕高度（px）
     */
    private fun screenHeight(context: Context = BaseApplication.instance): Int {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            context.resources.displayMetrics.heightPixels
        } else {
            context.resources.displayMetrics.widthPixels
        }
    }

    /**
     * 屏幕比值
     */
    private fun screenDensity(context: Context = BaseApplication.instance): Int {
        return context.resources.displayMetrics.densityDpi
    }

    /**
     * 设计图宽度转实际宽度
     */
    fun getRealSize(length: Int): Int {
        return if (length > 0) {
            (length * screenWidth.toDouble() / designWidth).toInt().min(1)
        } else {
            0
        }
    }

    fun getRealSize(length: Double): Int {
        return if (length > 0) {
            (length * screenWidth.toDouble() / designWidth).toInt().min(1)
        } else {
            0
        }
    }

    /**
     * 设计图宽度转实际宽度
     */
    fun getRealSize(context: Context, length: Int): Int {
        return length * screenWidth(context) / designWidth
    }

    fun getRealSize(context: Context, length: Double): Int {
        return (length * screenWidth(context).toDouble() / designWidth).toInt()
    }

    /**
     * 设计图宽度转实际宽度
     */
    fun getRealSizeFloat(context: Context, length: Int): Float {
        return getRealSizeFloat(context, length.toFloat())
    }

    fun getRealSizeFloat(context: Context, length: Float): Float {
        return length * screenWidth(context).toFloat() / designWidth.toFloat()
    }

    /**
     * 是否具备底部导航栏
     */
    fun hasNavigationBar(context: Context): Boolean {
        val appUsableSize = getAppUsableScreenSize(context)
        val realScreenSize = getRealScreenSize(context)
        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) return true
        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) return true
        return false
    }

    private fun getAppUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size
    }

    private fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getRealSize(size)
        return size
    }

}

/**
 * 全屏展示
 */
fun Window.fullScreen() {
    val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    decorView.systemUiVisibility = uiOptions
//    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val lp = attributes
        /**
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT 全屏模式，内容下移，非全屏不受影响
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES 允许内容区域延伸到刘海区
         * LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER 不允许内容延伸进刘海区
         */
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        attributes = lp
    }
}