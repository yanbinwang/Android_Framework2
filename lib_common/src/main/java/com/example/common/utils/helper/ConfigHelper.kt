package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
@SuppressLint("StaticFieldLeak")
object ConfigHelper {
    private lateinit var context: Context

    @JvmStatic
    fun initialize(application: Application) {
        context = application
    }

    // <editor-fold defaultstate="collapsed" desc="调取方法">
    /**
     * 在进程中去寻找当前APP的信息，判断是否在运行
     * 100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
     */
    fun isAppOnForeground(): Boolean {
        val processes = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses ?: return false
        for (process in processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(context.packageName)) return true
        }
        return false
    }

    /**
     * 获取当前app version code
     */
    fun getAppVersionCode(): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = context.applicationContext.packageManager.getPackageInfo(context.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    fun getAppVersionName(): String {
        var appVersionName = ""
        try {
            val packageInfo = context.applicationContext.packageManager.getPackageInfo(context.packageName, 0)
            appVersionName = packageInfo.versionName
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return appVersionName
    }

    /**
     * 获取当前app 包名
     */
    fun getPackageName(): String {
        return context.packageName
    }

    /**
     * 获取当前app 名称
     */
    @Synchronized
    fun getAppName(): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val labelRes = packageInfo.applicationInfo.labelRes
            return context.resources.getString(labelRes)
        } catch (_: Exception) {
        }
        return ""
    }
    // </editor-fold>

}