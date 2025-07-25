package com.example.common.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.example.common.BaseApplication
import com.example.common.utils.function.getManifestString
import com.example.framework.utils.function.value.min
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.background
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
fun Window.applyFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // 安卓11+ 现代全屏方案（移除重复的layoutInDisplayCutoutMode设置）
        setDecorFitsSystemWindows(false)
        insetsController?.let { controller ->
            // 隐藏系统栏（安卓15中Type.systemBars()已包含statusBars和navigationBars）
            controller.hide(WindowInsets.Type.systemBars())
            // 滑动时临时显示系统栏（保持原有行为）
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        /**
         * SYSTEM_UI_FLAG_LAYOUT_STABLE：保持布局稳定（避免状态栏 / 导航栏隐藏时布局跳动）
         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：布局延伸至状态栏区域
         * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：布局延伸至导航栏区域
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏导航栏
         * SYSTEM_UI_FLAG_FULLSCREEN：隐藏状态栏
         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY：进入「粘性沉浸式模式」：当用户从屏幕边缘滑动时，状态栏和导航栏会临时显示（半透明），几秒后自动隐藏，不会触发 OnSystemUiVisibilityChangeListener 回调。
         */
        var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//        // API 23+：若背景为浅色，设置状态栏文字为深色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
        decorView.systemUiVisibility = flags
        // 针对安卓4.4-9，强制设置导航栏透明（覆盖厂商默认）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            navigationBarColor = Color.TRANSPARENT
        }
    }
}

fun Window.setupNavigationBarPadding(navBarColorRes: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // 先设置背景色（确保初始状态正确）
        decorView.background(navBarColorRes)
        // 1. 监听视图附加到窗口（延迟获取Insets，确保数据准备好）
        decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                // 视图已附加到窗口，此时rootWindowInsets有效
                val initialInsets = v.rootWindowInsets
                val initialNavBottom = initialInsets.getInsets(WindowInsets.Type.navigationBars()).bottom
                // 设置初始padding
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, initialNavBottom)
                // 移除监听器（只需要执行一次）
                v.removeOnAttachStateChangeListener(this)
            }

            override fun onViewDetachedFromWindow(v: View) {}
        })
        // 2. 监听Insets变化（处理动态更新）
        decorView.setOnApplyWindowInsetsListener { v, insets ->
            // 仅在导航栏可见时设置内边距
            val navBottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            if (v.paddingBottom != navBottom) { // 只有变化时才更新
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBottom)
            }
            // 默认情况下都是白的
            v.background(navBarColorRes)
            // 避免重复处理
            insets
        }
    }
}