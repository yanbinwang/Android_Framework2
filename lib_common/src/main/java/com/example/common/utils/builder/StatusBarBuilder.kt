package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.common.R

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
    fun setHideStatus() = window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    /**
     * 透明状态栏(白电池)
     */
    fun setTransparentStatus() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }
        setMiuiStatusBarLightMode(false)
        setFlymeStatusBarLightMode(false)
    }

    /**
     * 透明状态栏(黑电池)
     */
    fun setTransparentDarkStatus() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
        setMiuiStatusBarLightMode(true)
        setFlymeStatusBarLightMode(true)
    }

    /**
     * 设置样式兼容（透明样式）
     */
    fun setTransparent(dark: Boolean, arrow: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || arrow) {
            if (dark) setTransparentDarkStatus() else setTransparentStatus()
        } else {
            setStatusBarColor(ContextCompat.getColor(window.context, R.color.black))
        }
    }

    /**
     * 设置状态栏颜色
     */
    fun setStatusBarColor(colorId: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) window.statusBarColor = colorId else window.statusBarColor = Color.BLACK

    /**
     * 状态栏黑色UI(只处理安卓6.0+的系统)
     */
    fun setStatusBarLightMode(dark: Boolean) {
        //如果大于7.0的系统，国内已经兼容谷歌黑电池的架构
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setNormalStatusBarLightMode(dark)
        } else {
            //如果是6.0的系统，小米魅族有不同的处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setNormalStatusBarLightMode(dark)
                setMiuiStatusBarLightMode(dark)
                setFlymeStatusBarLightMode(dark)
            }
        }
    }

    /**
     * 原生状态栏操作
     */
    private fun setNormalStatusBarLightMode(dark: Boolean) {
        val decorView = window.decorView
        var vis = decorView.systemUiVisibility
        vis = if (dark) vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        decorView.systemUiVisibility = vis
    }

    /**
     * 设置状态栏字体图标，需要MIUIV6以上
     */
    private fun setMiuiStatusBarLightMode(dark: Boolean) {
        val clazz = window.javaClass
        try {
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            extraFlagField.invoke(window, if (dark) darkModeFlag else 0, darkModeFlag)  //状态栏透明且黑色字体/清除黑色字体
        } catch (ignored: Exception) {
        }
    }

    /**
     * 设置状态栏图标和魅族特定的文字风格 可以用来判断是否为Flyme用户
     */
    private fun setFlymeStatusBarLightMode(dark: Boolean) {
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (dark) value or bit else value and bit.inv()
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (ignored: Exception) {
        }
    }

}