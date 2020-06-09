package com.example.framework.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.lang.ref.WeakReference

/**
 * author: wyb
 * date: 2017/9/9.
 * 导航栏工具类（）
 * 从5.0+开始兼容色值
 */
@SuppressLint("PrivateApi","InlinedApi")
class StatusBarUtil(activity: Activity) {
    //弱应用传入的activity
    private val mActivity : WeakReference<Activity> = WeakReference(activity)

    //隐藏导航栏
    fun setHideStatus() {
        mActivity.get()!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mActivity.get()!!.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    //透明状态栏(白电池)
    fun setTransparentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = mActivity.get()!!.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            setMiuiStatusBarLightMode(window, false)
            setFlymeStatusBarLightMode(window, false)
        }
    }

    //透明状态栏(黑电池)
    fun setTransparentDarkStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = mActivity.get()!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
            setMiuiStatusBarLightMode(window, true)
            setFlymeStatusBarLightMode(window, true)
        }
    }

    //设置状态栏颜色
    fun setStatusBarColor(colorId: Int) {
        val window = mActivity.get()!!.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = colorId
        } else {
            //统一兼容，6.0以下全部走黑电池
            window.statusBarColor = Color.BLACK
        }
    }

    //状态栏黑色UI(只处理安卓6.0+的系统)
    fun setStatusBarLightMode(isDark: Boolean) {
        val window = mActivity.get()!!.window
        //如果大于7.0的系统，国内已经兼容谷歌黑电池的架构
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setNormalStatusBarLightMode(window, isDark)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setNormalStatusBarLightMode(window, isDark)
                //如果是6.0的系统，小米魅族有不同的处理
                setMiuiStatusBarLightMode(mActivity.get()!!.window, isDark)
                setFlymeStatusBarLightMode(mActivity.get()!!.window, isDark)
            }
        }
    }

    //原生状态栏操作
    private fun setNormalStatusBarLightMode(window: Window, isDark: Boolean) {
        val decorView = window.decorView
        var vis = decorView.systemUiVisibility
        vis = if (isDark) {
            vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = vis
    }

    //设置状态栏字体图标，需要MIUIV6以上
    private fun setMiuiStatusBarLightMode(window: Window, isDark: Boolean) {
        val clazz = window.javaClass
        try {
            val darkModeFlag: Int
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            if (isDark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag)  //状态栏透明且黑色字体
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag)   //清除黑色字体
            }
        } catch (ignored: Exception) {
        }

    }

    //设置状态栏图标和魅族特定的文字风格 可以用来判断是否为Flyme用户
    private fun setFlymeStatusBarLightMode(window: Window, isDark: Boolean) {
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (isDark) {
                value or bit
            } else {
                value and bit.inv()
            }
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (ignored: Exception) {
        }
    }

}
