package com.example.common.utils.helper

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.example.common.BaseApplication
import com.example.common.config.CacheData.privacyAgreed
import com.example.common.config.Constants
import com.example.framework.utils.function.value.toSafeLong
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
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
        return processes.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == getPackageName() }
    }

    /**
     * @param pid 查所有进程。传具体 PID 则只查该进程；传 0 表示不限进程，返回该包名下所有历史进程的退出记录
     * @param maxNum 最多返回条数。系统按时间倒序返回最近 N 条，传 10 就是拿最近 10 次退出记录
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun getANRInfo(pid: Int = 0, maxNum: Int = 10): List<ApplicationExitInfo> {
        return withContext(IO) {
            val exitInfos = (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.getHistoricalProcessExitReasons(getPackageName(), pid, maxNum).orEmpty()
            exitInfos.filter { it.reason == ApplicationExitInfo.REASON_ANR }
        }
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
     * 获取当前应用的图标
     */
    fun getAppIcon(): Bitmap? {
        return try {
            context.packageManager.getApplicationIcon(Constants.APPLICATION_ID).let { drawable ->
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                require(width > 0 && height > 0) {
                    "Invalid icon intrinsic size: ${width}x${height}"
                }
                // targetSdk 37: 必须使用 ARGB_8888，RGB_565 已被 Canvas 绘制管线弃用
                val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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