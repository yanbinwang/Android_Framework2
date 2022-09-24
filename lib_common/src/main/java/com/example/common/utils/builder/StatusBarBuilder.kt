package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.common.R
import com.example.common.constant.Constants

/**
 * author: wyb
 * date: 2017/9/9.
 * 导航栏工具类（）
 * 从5.0+开始兼容色值
 */
@SuppressLint("PrivateApi", "InlinedApi")
class StatusBarBuilder(var window: Window) {

    /**
     * 隐藏导航栏
     */
    fun hideStatusBar() = window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    /**
     * 透明状态栏(白电池)
     */
    fun transparentStatusBar() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }
        miuiStatusBarLightMode(false)
        flymeStatusBarLightMode(false)
    }

    /**
     * 透明状态栏(黑电池)
     */
    fun transparentLightStatusBar() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
        miuiStatusBarLightMode(true)
        flymeStatusBarLightMode(true)
    }

    /**
     * 设置样式兼容（透明样式）
     */
    fun transparent(light: Boolean, enable: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || enable) {
            if (light) transparentLightStatusBar() else transparentStatusBar()
        } else {
            statusBarColor(ContextCompat.getColor(window.context, R.color.black))
        }
    }

    /**
     * 设置状态栏颜色
     */
    fun statusBarColor(colorId: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) window.statusBarColor = colorId else window.statusBarColor = Color.BLACK

    /**
     * 状态栏黑色UI(只处理安卓6.0+的系统)
     */
    fun statusBarLightMode(light: Boolean) {
        //如果大于7.0的系统，国内已经兼容谷歌黑电池的架构
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            normalStatusBarLightMode(light)
        } else {
            //如果是6.0的系统，小米魅族有不同的处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                normalStatusBarLightMode(light)
                miuiStatusBarLightMode(light)
                flymeStatusBarLightMode(light)
            }
        }
    }

    /**
     * 原生状态栏操作
     */
    private fun normalStatusBarLightMode(light: Boolean) {
        val decorView = window.decorView
        var vis = decorView.systemUiVisibility
        vis = if (light) vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        decorView.systemUiVisibility = vis
    }

    /**
     * 设置状态栏字体图标，需要MIUIV6以上
     */
    private fun miuiStatusBarLightMode(light: Boolean) {
        val clazz = window.javaClass
        try {
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            extraFlagField.invoke(window, if (light) darkModeFlag else 0, darkModeFlag)  //状态栏透明且黑色字体/清除黑色字体
        } catch (ignored: Exception) {
        }
    }

    /**
     * 设置状态栏图标和魅族特定的文字风格 可以用来判断是否为Flyme用户
     */
    private fun flymeStatusBarLightMode(light: Boolean) {
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (light) value or bit else value and bit.inv()
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (ignored: Exception) {
        }
    }

}

/**
 * 空出状态栏高度
 */
fun View.statusBarHeight() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        layoutParams = when (parent) {
            is LinearLayout -> LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            is RelativeLayout -> RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            is FrameLayout -> FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            else -> ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
        }
    }
}

fun View.statusBarPadding() = run {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setPadding(0, Constants.STATUS_BAR_HEIGHT, 0, 0)
}

fun View.statusBarMargin(enable: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || enable) {
        val params = when (parent) {
            is LinearLayout -> layoutParams as LinearLayout.LayoutParams
            is RelativeLayout -> layoutParams as RelativeLayout.LayoutParams
            is FrameLayout -> layoutParams as FrameLayout.LayoutParams
            else -> layoutParams as ConstraintLayout.LayoutParams
        }
        params.topMargin = Constants.STATUS_BAR_HEIGHT
        layoutParams = params
    }
}