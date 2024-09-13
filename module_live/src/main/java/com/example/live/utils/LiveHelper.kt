package com.example.live.utils

import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.common.config.Constants
import com.example.common.utils.builder.shortToast
import com.example.common.utils.helper.AccountHelper
import com.example.live.service.KeepAppAliveService
import com.qiniu.pili.droid.streaming.StreamingEnv
import com.qiniu.pili.droid.streaming.common.FileLogHelper
import xcrash.TombstoneManager
import xcrash.XCrash
import xcrash.XCrash.InitParameters
import java.io.File

/**
 * 直播库初始化
 */
object LiveHelper {
    private var initialized = false
    private var mIsServiceAlive = false
    private var mServiceIntent: Intent? = null
    private var mContext: Application? = null
    private val mCrashLogDir get() = mContext?.getExternalFilesDir(null)?.absolutePath ?: ""

    /**
     * 注意：参数 userId 代表用户的唯一标识符，用于区分不同的用户
     * 用户登录时主动调取/application中也做一次调取
     */
    @JvmStatic
    fun init(application: Application) {
        mContext = application
        if (AccountHelper.isLogin()) {
            //初始化推送
            StreamingEnv.init(application.applicationContext, AccountHelper.getUserId())
            //设置日志等级
            StreamingEnv.setLogLevel(Log.INFO)
            //开启日志的本地保存，保存在应用私有目录(getExternalFilesDir) 或者 getFilesDir 文件目录下的 Pili 文件夹中
            //默认为关闭
            StreamingEnv.setLogfileEnabled(true)
            //保活
            AppStateTracker.track(application, object : AppStateTracker.AppStateChangeListener {
                override fun appTurnIntoForeground() {
                    stopService()
                }

                override fun appTurnIntoBackGround() {
                    startService()
                }

                override fun appDestroyed() {
                    stopService()
                }
            })
            //开启日志收集
            openCrash()
        }
    }

    internal fun startService() {
        if (mServiceIntent == null) {
            mServiceIntent = Intent(mContext, KeepAppAliveService::class.java)
        }
        mContext?.startService(mServiceIntent)
        mIsServiceAlive = true
    }

    internal fun stopService() {
        if (mIsServiceAlive) {
            mContext?.stopService(mServiceIntent)
            mServiceIntent = null
            mIsServiceAlive = false
        }
    }

    /**
     * application种attachBaseContext调取
     */
    private fun openCrash() {
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
                        "崩溃日志已上传！".shortToast()
                    }

                    override fun onReportError(name: String?, errorMsg: String?) {
                        "崩溃日志上传失败 : $errorMsg".shortToast()
                    }
                })
            }
        }
    }

}