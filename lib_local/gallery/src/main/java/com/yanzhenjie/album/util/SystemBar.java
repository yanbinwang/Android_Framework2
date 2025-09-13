package com.yanzhenjie.album.util;

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
 * Created by YanZhenjie on 2018/4/10.
 */
public class SystemBar {

    /**
     * Set the status bar color.
     */
    public static void setStatusBarColor(Activity activity, int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setStatusBarColor(activity.getWindow(), statusBarColor);
    }

    /**
     * Set the status bar color.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Window window, int statusBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
    }

    /**
     * Set the navigation bar color.
     */
    public static void setNavigationBarColor(Activity activity, int navigationBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setNavigationBarColor(activity.getWindow(), navigationBarColor);
    }

    /**
     * Set the navigation bar color.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(Window window, int navigationBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(navigationBarColor);
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    public static void invasionStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invasionStatusBar(activity.getWindow());
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void invasionStatusBar(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * Set the content layout full the NavigationBar, but do not hide NavigationBar.
     */
    public static void invasionNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invasionNavigationBar(activity.getWindow());
    }

    /**
     * Set the content layout full the NavigationBar, but do not hide NavigationBar.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void invasionNavigationBar(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    /**
     * Set the status bar to dark.
     */
    public static boolean setStatusBarDarkFont(Activity activity, boolean darkFont) {
        return setStatusBarDarkFont(activity.getWindow(), darkFont);
    }

    /**
     * Set the status bar to dark.
     */
    public static boolean setStatusBarDarkFont(Window window, boolean darkFont) {
        if (setMIUIStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont);
            return true;
        } else if (setMeizuStatusBarFont(window, darkFont)) {
            setDefaultStatusBarFont(window, darkFont);
            return true;
        } else {
            return setDefaultStatusBarFont(window, darkFont);
        }
    }

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