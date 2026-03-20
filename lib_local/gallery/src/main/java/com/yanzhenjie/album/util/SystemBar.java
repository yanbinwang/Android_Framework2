package com.yanzhenjie.album.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 系统状态栏/导航栏工具类
 * 功能：设置状态栏颜色、导航栏颜色、沉浸式、深色状态栏文字（兼容小米、魅族、原生安卓）
 */
public class SystemBar {

    /**
     * 设置状态栏颜色
     */
    public static void setStatusBarColor(Activity activity, int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColor(activity.getWindow(), statusBarColor);
        }
    }

    /**
     * 设置状态栏颜色（LOLLIPOP以上）
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Window window, int statusBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
    }

    /**
     * 设置导航栏颜色
     */
    public static void setNavigationBarColor(Activity activity, int navigationBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setNavigationBarColor(activity.getWindow(), navigationBarColor);
        }
    }

    /**
     * 设置导航栏颜色（LOLLIPOP以上）
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(Window window, int navigationBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(navigationBarColor);
    }

    /**
     * 布局侵入状态栏（沉浸式，不隐藏状态栏）
     */
    public static void invasionStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invasionStatusBar(activity.getWindow());
        }
    }

    /**
     * 布局侵入状态栏（LOLLIPOP以上）
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void invasionStatusBar(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 布局侵入导航栏（沉浸式）
     */
    public static void invasionNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invasionNavigationBar(activity.getWindow());
        }
    }

    /**
     * 布局侵入导航栏（LOLLIPOP以上）
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void invasionNavigationBar(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    /**
     * 设置状态栏文字为深色/浅色
     */
    public static boolean setStatusBarDarkFont(Activity activity, boolean darkFont) {
        return setStatusBarDarkFont(activity.getWindow(), darkFont);
    }

    /**
     * 统一设置深色状态栏字体（兼容：原生、小米MIUI、魅族Flyme）
     */
    public static boolean setStatusBarDarkFont(Window window, boolean darkFont) {
        // 先尝试适配小米
        if (setMIUIStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont);
            return true;
            // 再尝试适配魅族
        } else if (setMeizuStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont);
            return true;
            // 最后用原生安卓方案
        } else {
            return setDefaultStatusBarFont(window, darkFont);
        }
    }

    /**
     * 魅族手机状态栏深色字体适配
     */
    @SuppressLint("PrivateApi")
    private static boolean setMeizuStatusBarFont(Window window, boolean darkFont) {
        try {
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkFont) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 小米MIUI手机状态栏深色字体适配
     */
    @SuppressLint("PrivateApi")
    private static boolean setMIUIStatusBarFont(Window window, boolean dark) {
        Class<?> clazz = window.getClass();
        try {
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 原生Android 6.0+ 深色字体方案
     */
    private static boolean setDefaultStatusBarFont(Window window, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            if (dark) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            return true;
        }
        return false;
    }

}