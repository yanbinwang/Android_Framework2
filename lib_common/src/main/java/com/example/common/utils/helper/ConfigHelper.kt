package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.common.BaseApplication
import com.example.common.config.CacheData.privacyAgreed
import com.example.framework.utils.function.value.toSafeLong

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
@SuppressLint("StaticFieldLeak")
object ConfigHelper {
    private val context by lazy { BaseApplication.instance.applicationContext }
    private val packageInfo by lazy { context.packageManager.getPackageInfo(getPackageName(), 0) }

    // <editor-fold defaultstate="collapsed" desc="调取方法">
    /**
     * 是否同意告知书
     */
    var isPrivacyPolicyAccepted: Boolean
        get() = privacyAgreed.get()
        set(value) {
            privacyAgreed.set(value)
        }

    /**
     * 检查 App 是否处于真正的前台交互状态
     * 此方法仅判断前台交互性，不等同于 UI 可见性 (例如被半透明 Activity 覆盖时返回 false)
     */
    fun isAppInForeground(): Boolean {
        val processes = (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses ?: return false
        return processes.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == context.packageName }
    }

    /**
     * 获取当前应用的 versionCode
     */
    fun getAppVersionCode(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toSafeLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * 获取当前应用的 versionName
     */
    fun getAppVersionName(): String {
        return try {
            packageInfo.versionName.orEmpty()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取当前应用的名称
     */
    fun getAppName(): String {
        return try {
            val labelRes = packageInfo.applicationInfo?.labelRes ?: return ""
            context.resources.getString(labelRes)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取当前应用的包名
     */
    fun getPackageName(): String {
        return context.packageName
    }
    // </editor-fold>

}