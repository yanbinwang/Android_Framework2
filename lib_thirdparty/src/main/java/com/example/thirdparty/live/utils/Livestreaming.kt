package com.example.thirdparty.live.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.common.config.Constants
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.file.deleteDir
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.logI
import com.example.qiniu.utils.AppStateTracker
import com.example.thirdparty.live.service.KeepAppAliveService
import com.qiniu.pili.droid.streaming.StreamingEnv
import com.qiniu.pili.droid.streaming.common.FileLogHelper
import xcrash.TombstoneManager
import xcrash.XCrash
import xcrash.XCrash.InitParameters
import java.io.File

/**
 * 直播库初始化
 * 1.遇到app隐私协议问题，会在调取initPrivacyAgreed后调取init方法
 * 2.如需接入日志上报，开启XCrash的依赖
 */
object Livestreaming {
    private var initialized = false
    private var mIsServiceAlive = false
    private var mServiceIntent: Intent? = null
    private val mCrashLogDir get() = getStoragePath("崩溃日志", false)

    /**
     * 注意：参数 userId 代表用户的唯一标识符，用于区分不同的用户
     * application中做一次调取，如果隐私协议未启用则延后调取
     */
    @JvmStatic
    fun init(application: Application, crashEnabled: Boolean = true) {
        //初始化直播/获取当前登录用户的userid，未登录sdk内部会随机产生一组uid
        StreamingEnv.init(application.applicationContext, AccountHelper.getUserId().ifEmpty { "" })
        //设置日志等级
        StreamingEnv.setLogLevel(Log.INFO)
        //开启日志的本地保存，保存在应用私有目录(getExternalFilesDir) 或者 getFilesDir 文件目录下的 Pili 文件夹中
        if (crashEnabled) {
            //默认为关闭
            StreamingEnv.setLogfileEnabled(true)
            //开启日志收集
            openCrash(application)
        }
        //保活服务/<service android:name="com.example.thirdparty.live.service.KeepAppAliveService" />
        AppStateTracker.track(application, object : AppStateTracker.AppStateChangeListener {
            override fun appTurnIntoForeground() {
                stopService(application)
            }

            override fun appTurnIntoBackGround() {
                startService(application)
            }

            override fun appDestroyed() {
                stopService(application)
            }
        })
    }

    private fun openCrash(mContext: Context) {
        if (!initialized) {
            initialized = true
            XCrash.init(mContext, InitParameters()
                //设置log日志位置
                .setLogDir(mCrashLogDir)
                .setJavaDumpNetworkInfo(false)
                .setNativeDumpNetwork(false)
                .setNativeDumpAllThreads(false)
                .setAppVersion(Constants.VERSION_NAME))
            checkToUploadCrashFiles()
        }
    }

    private fun checkToUploadCrashFiles() {
        val crashFolder = File(mCrashLogDir)
        val crashFiles = crashFolder.listFiles() ?: return
        crashFiles.forEach { crashFile ->
            if (crashFile.isFile()) {
                StreamingEnv.reportLogFileByPath(crashFile.path, object : FileLogHelper.LogReportCallback {
                    override fun onReportSuccess(logNames: MutableList<String>?) {
                        logNames?.forEach { logName ->
                            if (logName == crashFile.name) {
                                TombstoneManager.deleteTombstone(crashFile.path)
                            }
                        }
                        "崩溃日志已上传！".logI
                    }

                    override fun onReportError(name: String?, errorMsg: String?) {
                        "崩溃日志上传失败 : $errorMsg".logI
                    }
                })
            }
        }
    }

    private fun startService(mContext: Context) {
        if (mServiceIntent == null) {
            mServiceIntent = Intent(mContext, KeepAppAliveService::class.java)
        }
        mContext.startService(mServiceIntent)
        mIsServiceAlive = true
    }

    private fun stopService(mContext: Context) {
        if (mIsServiceAlive) {
            mContext.stopService(mServiceIntent)
            mServiceIntent = null
            mIsServiceAlive = false
        }
    }

    /**
     * 主动上报日志，默认位置是七牛云自己在android/包名文件夹下创建的Pili文件
     * sdk并未提供清空的api，故而主动上报后做一次清空
     */
    @JvmStatic
    fun reportLog() {
        StreamingEnv.reportLogFiles(object : FileLogHelper.LogReportCallback {
            override fun onReportSuccess(logNames: MutableList<String>?) {
                if (logNames.safeSize == 0) {
                    return
                }
                logNames?.forEach { it.logI }
                //日志上传后清空
                StreamingEnv.getLogFilePath().deleteDir()
                "日志已上传".logI
            }

            override fun onReportError(name: String?, errorMsg: String?) {
                "日志 $name 上传失败: $errorMsg".logI
            }
        })
    }

    /**
     * 用户登录退出时候调取
     * 如果uid为空，sdk内部会通过随机数自动产生一个
     */
    @JvmStatic
    fun updateUid(uid: String? = "") {
        StreamingEnv.updateUid(uid)
    }

}