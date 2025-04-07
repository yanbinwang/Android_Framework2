package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.common.BaseApplication
import com.example.common.config.CacheData
import com.example.common.config.CacheData.privacyAgreed
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeLong

/**
 *  Created by wangyanbin
 *  应用配置工具类
 *  application中尽量做一些第三方和项目工具的初始化，取值赋值静态变量容易丢失
 *  可以注入一个application，然后需要的时候再去调取方法取
 */
@SuppressLint("StaticFieldLeak")
object ConfigHelper {
    private val mContext by lazy { BaseApplication.instance.applicationContext }

    // <editor-fold defaultstate="collapsed" desc="调取方法">
    /**
     * 存储是否已经同意告知书
     */
    fun setPrivacyAgreed(value: Boolean) {
        privacyAgreed.set(value)
    }

    fun getPrivacyAgreed(): Boolean {
        return privacyAgreed.get()
    }

    /**
     * 在进程中去寻找当前APP的信息，判断是否在运行
     * 100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
     */
    fun appIsOnForeground(): Boolean {
        val processes = (mContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses ?: return false
        for (process in processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(mContext.packageName)) return true
        }
        return false
    }

    /**
     * 获取当前app version code
     */
    fun getAppVersionCode(): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = mContext.applicationContext.packageManager.getPackageInfo(mContext.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toSafeLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    fun getAppVersionName(): String {
        var appVersionName = ""
        try {
            val packageInfo = mContext.applicationContext.packageManager.getPackageInfo(mContext.packageName, 0)
            appVersionName = packageInfo.versionName.orEmpty()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionName
    }

    /**
     * 获取当前app 名称
     */
    fun getAppName(): String {
        try {
            val packageInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
            val labelRes = packageInfo.applicationInfo?.labelRes.orZero
            return mContext.resources.getString(labelRes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 获取当前app 包名
     */
    fun getPackageName(): String {
        return mContext.packageName
    }
    // </editor-fold>

}