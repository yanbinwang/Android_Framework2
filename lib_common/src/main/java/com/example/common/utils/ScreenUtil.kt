package com.example.common.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.common.BaseApplication
import com.example.common.utils.function.getManifestString
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.value.min
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
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
    private val designWidth by lazy { getManifestString("design_width_in_dp").toSafeInt(750) }

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
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    private fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size
    }

    /**
     * 全屏展示
     */
    fun Window.fullScreen() {
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            attributes = lp
        }
    }

//    /**
//     * 设置view高度为导航栏高度
//     * 手动添加一个view，高度设为wrap
//     */
//    fun View.statusBarHeight() {
//        getStatusBarHeight().also {
//            layoutParams = when (parent) {
//                is LinearLayout -> LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, it)
//                is RelativeLayout -> RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, it)
//                is FrameLayout -> FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, it)
//                else -> ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, it)
//            }
//        }
//    }

    /**
     * 设置view底部所有子控件居下导航栏高度
     */
    fun View.statusBarPadding() { padding(top = getStatusBarHeight()) }

    /**
     * 设置view整体向上导航栏高度
     */
    fun View.statusBarMargin() {
//        val params = when (parent) {
//            is LinearLayout -> layoutParams as LinearLayout.LayoutParams
//            is RelativeLayout -> layoutParams as RelativeLayout.LayoutParams
//            is FrameLayout -> layoutParams as FrameLayout.LayoutParams
//            else -> layoutParams as ConstraintLayout.LayoutParams
//        }
//        params.topMargin = getStatusBarHeight()
//        layoutParams = params
        margin(top = getStatusBarHeight())
    }

}