package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
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
     * 存储是否已经同意告知书
     */
    var isPrivacyPolicyAccepted: Boolean
        get() = privacyAgreed.get()
        set(value) {
            privacyAgreed.set(value)
        }

    /**
     * 在进程中去寻找当前APP的信息，判断是否在运行
     * 100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
     */
    fun appIsOnForeground(): Boolean {
//        val processes = (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses ?: return false
//        for (process in processes) {
//            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(context.packageName)) return true
//        }
//        return false
        /**
         * RESUMED : 前台，用户正在交互 -> true
         * STARTED : 前台，但被透明Activity/对话框遮挡 -> true
         * CREATED : 后台，所有页面不可见 -> false
         * INITIALIZED : 冷启动瞬间，尚未分发 -> false
         */
        return ProcessLifecycleOwner.get().lifecycle.currentState >= Lifecycle.State.STARTED
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