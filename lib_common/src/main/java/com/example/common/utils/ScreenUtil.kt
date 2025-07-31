package com.example.common.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.function.color
import com.example.common.utils.function.getManifestString
import com.example.framework.utils.function.value.min
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.math.max

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

//    private fun getAppUsableScreenSize(context: Context): Point {
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
//        val display = windowManager?.defaultDisplay
//        val size = Point()
//        display?.getSize(size)
//        return size
//    }
//
//    private fun getRealScreenSize(context: Context): Point {
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
//        val display = windowManager?.defaultDisplay
//        val size = Point()
//        display?.getRealSize(size)
//        return size
//    }

    /**
     * 获取应用实际可用的屏幕尺寸（即扣除状态栏、导航栏等系统栏后的区域）
     */
    private fun getAppUsableScreenSize(context: Context): Point {
        // API 30+：用 WindowMetrics + WindowInsets 计算可用区域（替代 display.getSize()）
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val currentMetrics = windowManager?.currentWindowMetrics
            // 1. 获取当前窗口的整体边界（包含系统栏）
            val bounds = currentMetrics?.bounds
            // 2. 获取系统栏（状态栏、导航栏、显示切口）的 insets（遮挡区域）
            val insets = currentMetrics?.windowInsets?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            // 3. 从整体边界中减去系统栏尺寸 → 得到应用可用区域（与旧版 getSize() 一致）
            val usableWidth = bounds?.width().orZero - (insets?.left.orZero + insets?.right.orZero)
            val usableHeight = bounds?.height().orZero - (insets?.top.orZero + insets?.bottom.orZero)
            Point(usableWidth, usableHeight)
        } else {
            // API 23-29：沿用旧版 getSize()
            val display = windowManager?.defaultDisplay
            val size = Point()
            // 避免 null 导致尺寸为 (0,0) 以外的异常值
            display?.getSize(size) ?: size.set(0, 0)
            size
        }
    }

    /**
     * 获取真实屏幕尺寸
     */
    private fun getRealScreenSize(context: Context): Point {
        // API 30：获取真实屏幕最大尺寸（替代 display.getRealSize()）
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 获取最大窗口尺寸的 metrics
            val maxWindowMetrics = windowManager?.maximumWindowMetrics
            // 获取当前窗口真实物理尺寸
            val rect = maxWindowMetrics?.bounds
            Point(rect?.width().orZero, rect?.height().orZero)
        } else {
            // API 23-29：沿用旧版 getRealSize() 获取屏幕真实物理尺寸
            val display = windowManager?.defaultDisplay
            val size = Point()
            // 避免 null 导致尺寸为 (0,0) 以外的异常值
            display?.getRealSize(size) ?: size.set(0, 0)
            size
        }
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

fun Window.setNavigationBarDrawable(navigationBarColor: Int) {
//    // 1. 获取全局样式中的 windowBackground（作为底层背景）
//    val windowBackground = decorView.background ?: color(R.color.appWindowBackground).toDrawable()
//    // 2. 创建底部色块 Drawable
//    val bottomBarDrawable = NavigationBarDrawable(color(navigationBarColor))
//    // 3. 组合成 LayerDrawable（底层：windowBackground，上层：底部色块）
//    val combinedDrawable = LayerDrawable(arrayOf(windowBackground, bottomBarDrawable))
//    // 4. 设置为 decorView 背景（此时两者会叠加显示）
//    decorView.background = combinedDrawable
//    // 5. 监听视图附加到窗口（延迟获取Insets，确保数据准备好）
//    decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//        override fun onViewAttachedToWindow(v: View) {
//            // 视图已附加到窗口，此时rootWindowInsets有效
//            val initialNavBottom = getNavigationBarHeight()
////    val initialInsets = v.rootWindowInsets
////    val initialNavBottom = initialInsets.getInsets(WindowInsets.Type.navigationBars()).bottom
//            // 设置初始padding
//            v.padding(v.paddingLeft, v.paddingTop, v.paddingRight, initialNavBottom)
//            bottomBarDrawable.updateNavigationBarHeight(initialNavBottom)
//            // 移除监听器（只需要执行一次）
//            v.removeOnAttachStateChangeListener(this)
//        }
//
//        override fun onViewDetachedFromWindow(v: View) {}
//    })
//    // 6. 监听Insets变化（处理动态更新）
//    decorView.setOnApplyWindowInsetsListener { v, insets ->
//        // 仅在导航栏可见时设置内边距
//        val navBottom = getNavigationBarHeight()
////    val navBottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
//        // 只有变化时才更新
//        if (v.paddingBottom != navBottom) {
//            v.padding(v.paddingLeft, v.paddingTop, v.paddingRight, navBottom)
//            bottomBarDrawable.updateNavigationBarHeight(navBottom)
//        }
//        // 避免重复处理
//        insets.consumeSystemWindowInsets()
//    }
    // 1. 项目MinSdk为23，TargetSdk为36,底部包含背景/UI深浅两部分，API 23-25无法操作UI深浅，默认做成黑背景白电池
    val mNavigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarColor else R.color.bgBlack
    // 2. 获取样式中的 android:windowBackground 作为底层背景（Activity如果不单独设置style样式，默认采取的是全局背景色）
    val windowBackground = decorView.background?.let { background ->
        when (background) {
            // 纯颜色背景直接使用
            is ColorDrawable -> background
            // 图片类背景（如BitmapDrawable、VectorDrawable等）单独处理
            is BitmapDrawable, is VectorDrawable -> {
                // 保留图片背景或根据图片主题色动态适配，直接返回图片Drawable，或做其他处理
                background
            }
            // 其他未知类型Drawable默认处理
            else -> {
                null
            }
        }
    } ?: color(R.color.appWindowBackground).toDrawable()
    // 3. 创建底部色块 Drawable
    val bottomBarDrawable = NavigationBarDrawable(color(mNavigationBarColor))
    // 4. 组合成 LayerDrawable（上层：android:windowBackground，底层：底部色块）
    val combinedDrawable = LayerDrawable(arrayOf(windowBackground, bottomBarDrawable))
    // 5. 设置为 decorView 背景（此时两者会叠加显示）
    decorView.background = combinedDrawable
    // 6. 监听Insets变化（处理动态更新）
    ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
        // 1. 获取系统栏的尺寸（单位：px）
//        val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars()) // 状态栏高度
        val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars()) // 导航栏高度（含底部或侧边）
        // 2. 仅设置底部padding，其他方向保持不变
        val navBottom = navigationBarInsets.bottom
        if (v.paddingBottom != navBottom) {
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navigationBarInsets.bottom)
            bottomBarDrawable.updateNavigationBarHeight(navBottom)
        }
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * 导航栏背景绘制工具，用于在Edge-to-Edge模式下绘制底部导航栏区域背景
 * 支持动态更新导航栏高度，适配不同设备和屏幕旋转场景
 * navigationBarColor 底部色块颜色
 * navigationBarHeight 底部色块高度
 */
class NavigationBarDrawable(@ColorInt private val backgroundColor: Int, private var navigationBarHeight: Int = 0) : Drawable() {
    private val paint = Paint().apply {
        color = backgroundColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty || navigationBarHeight <= 0) return
        // 计算绘制区域
        val top = (bounds.height() - navigationBarHeight).toFloat()
        val bottom = bounds.height().toFloat()
        // 绘制底部色块
        if (top < bottom) {
            canvas.drawRect(bounds.left.toFloat(), top, bounds.right.toFloat(), bottom, paint)
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        val adjustedAlpha = alpha.coerceIn(0, 255)
        if (paint.alpha != adjustedAlpha) {
            paint.alpha = adjustedAlpha
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    fun updateNavigationBarHeight(height: Int) {
        val validHeight = max(0, height)
        if (navigationBarHeight != validHeight) {
            navigationBarHeight = validHeight
            invalidateSelf()
        }
    }

}