package com.yanzhenjie.album.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi

/**
 * Created by YanZhenjie on 2018/4/10.
 */
@SuppressLint("ObsoleteSdkInt")
object SystemBar {

    /**
     * Set the status bar color.
     */
    @JvmStatic
    fun setStatusBarColor(activity: Activity, statusBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setStatusBarColor(activity.window, statusBarColor)
    }

    /**
     * Set the status bar color.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun setStatusBarColor(window: Window, statusBarColor: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor
    }

    /**
     * Set the navigation bar color.
     */
    @JvmStatic
    fun setNavigationBarColor(activity: Activity, navigationBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setNavigationBarColor(activity.window, navigationBarColor)
    }

    /**
     * Set the navigation bar color.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun setNavigationBarColor(window: Window, navigationBarColor: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = navigationBarColor
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    @JvmStatic
    fun invasionStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) invasionStatusBar(activity.window)
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun invasionStatusBar(window: Window) {
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * Set the content layout full the NavigationBar, but do not hide NavigationBar.
     */
    @JvmStatic
    fun invasionNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) invasionNavigationBar(activity.window)
    }

    /**
     * Set the content layout full the NavigationBar, but do not hide NavigationBar.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun invasionNavigationBar(window: Window) {
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = Color.TRANSPARENT
    }

    /**
     * Set the status bar to dark.
     */
    @JvmStatic
    fun setStatusBarDarkFont(activity: Activity, darkFont: Boolean): Boolean {
        return setStatusBarDarkFont(activity.window, darkFont)
    }

    /**
     * Set the status bar to dark.
     */
    @JvmStatic
    fun setStatusBarDarkFont(window: Window, darkFont: Boolean): Boolean {
        if (setMIUIStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont)
            return true
        } else if (setMeizuStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont)
            return true
        } else {
            return setDefaultStatusBarFont(window, darkFont)
        }
    }

    private fun setMeizuStatusBarFont(window: Window, darkFont: Boolean): Boolean {
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (darkFont) {
                value or bit
            } else {
                value and bit.inv()
            }
            meizuFlags.setInt(lp, value)
            window.setAttributes(lp)
            return true
        } catch (_: Exception) {
        }
        return false
    }

    @SuppressLint("PrivateApi")
    private fun setMIUIStatusBarFont(window: Window, dark: Boolean): Boolean {
        val clazz: Class<*> = window.javaClass
        try {
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            if (dark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag)
            }
            return true
        } catch (_: Exception) {
        }
        return false
    }

    private fun setDefaultStatusBarFont(window: Window, dark: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            if (dark) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            return true
        }
        return false
    }

}