package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.core.Controller
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.common.R
import com.example.common.utils.MmkvUtil
import com.example.framework.utils.function.color
import java.lang.ref.WeakReference

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
@SuppressLint("MissingPermission", "HardwareIds", "StaticFieldLeak", "InternalInsetResource")
object ConfigHelper {
    private lateinit var context: Context

    @JvmStatic
    fun initialize(application: Application) {
        context = application
    }

    // <editor-fold defaultstate="collapsed" desc="公用方法">
    /**
     * 遮罩引导
     * https://www.jianshu.com/p/f28603e59318
     * ConfigHelper.showGuide(this,"sdd",GuidePage
     *  .newInstance()
     *  .addHighLight(binding.btnList)
     *  .setBackgroundColor(color(R.color.black_4c000000))
     *  .setLayoutRes(R.layout.view_guide_simple))
     */
    @JvmStatic
    fun showGuide(activity: Activity, label: String, vararg pages: GuidePage) {
        if (!MmkvUtil.decodeBool(label)) {
            MmkvUtil.encode(label, true)
            WeakReference(activity).get()?.apply {
                val builder = NewbieGuide.with(this)//传入activity
                    .setLabel(label)//设置引导层标示，用于区分不同引导层，必传！否则报错
                    .setOnGuideChangedListener(object : OnGuideChangedListener {
                        override fun onShowed(controller: Controller?) {
                        }

                        override fun onRemoved(controller: Controller?) {
                        }
                    })
                    .alwaysShow(true)
                for (page in pages) {
                    //此处处理一下阴影背景
                    page.backgroundColor = activity.color(R.color.black_4c000000)
                    builder.addGuidePage(page)
                }
                builder.show()
            }
        }
    }

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="私有调取方法">
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
            val packageManager = context.packageManager;
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val labelRes = packageInfo.applicationInfo.labelRes
            return context.resources.getString(labelRes)
        } catch (_: Exception) {
        }
        return ""
    }
    // </editor-fold>

}