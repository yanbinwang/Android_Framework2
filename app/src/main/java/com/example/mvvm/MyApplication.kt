package com.example.mvvm

import android.os.Looper
import android.util.Log
import com.amap.api.services.core.ServiceSettings
import com.example.common.BaseApplication
import com.example.common.config.Constants.VERSION_NAME
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.logE
import com.example.greendao.dao.DaoMaster
import com.example.mvvm.activity.MainActivity
import com.example.objectbox.dao.MyObjectBox
import com.example.thirdparty.media.album.GlideLoader
import com.example.thirdparty.media.oss.OssDBHelper
import com.example.thirdparty.media.oss.OssDBHelper2
import com.example.thirdparty.media.oss.OssFactory
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import com.zxy.recovery.callback.RecoveryCallback
import com.zxy.recovery.core.Recovery
import io.objectbox.BoxStore
import java.util.Locale

/**
 * Created by WangYanBin on 2020/8/14.
 * 1.如果三方库不依赖于common库，则需要在application中初始化的方法统一放在BaseApplication中
 * 2.如果依赖了common库，且在thirdparty中做了二次工具类的封装，此时若还需在application中初始化，放在MyApplication中
 */
class MyApplication : BaseApplication() {
    //数据库
    private lateinit var daoMaster: DaoMaster
    private lateinit var boxStore: BoxStore

    companion object {
        val instance: MyApplication
            get() = BaseApplication.instance as MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        if (isDebug) {
            initDebugging()
        } else {
            //当前若是发布包，接管系统loop，让用户感知不到程序闪退
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    println("AppCatch -${Log.getStackTraceString(e)}")
                }
            }
        }
        //初始化图片库类
        initAlbum()
        //数据库初始化
        initDao()
        initOssDao()
        //初始化oss
        initOss()
        //初始化进程监听
        setOnStateChangedListener { if (it) initOss() }
        //授权初始化
        setOnPrivacyAgreedListener {
            if (it) {
                initAMap()
            }
        }
        //初始化需要授权的库
        initPrivacyAgreed(false)
    }

    private fun initDebugging() {
        //闪退抓捕->不能和LeakCanary共存
        Recovery.getInstance()
            //debug	是否开启debug模式
            .debug(true)
            //recoverInBackground 当应用在后台时发生Crash，是否需要进行恢复
            .recoverInBackground(false)
            //recoverStack	是否恢复整个Activity Stack，否则将恢复栈顶Activity
            .recoverStack(true)
            //mainPage	回退的界面
            .mainPage(MainActivity::class.java)
            //callback	发生Crash时的回调
            .recoverEnabled(true)//发布版本不跳转
            .callback(object : RecoveryCallback {
                private val infoList = mutableListOf<String>()
                override fun stackTrace(stackTrace: String?) {
                    infoList.add("StackTrace:\n$stackTrace\n\n")
                }

                override fun cause(cause: String?) {
                    infoList.add("Cause:\n$cause\n\n")
                }

                override fun exception(throwExceptionType: String?, throwClassName: String?, throwMethodName: String?, throwLineNumber: Int) {
                    infoList.add("\nException:\nExceptionData{" +
                            "className='" + throwClassName + '\'' +
                            ", type='" + throwExceptionType + '\'' +
                            ", methodName='" + throwMethodName + '\'' +
                            ", lineNumber=" + throwLineNumber +
                            '}'
                    )
                }

                override fun throwable(throwable: Throwable?) {
                    val report = StringBuilder()
                    infoList.reverse()
                    infoList.forEach {
                        report.append(it)
                    }
                    infoList.clear()
                    ("————————————————————————应用崩溃————————————————————————" +
                            "${report}\n" +
                            " ").logE("LoggingInterceptor")
                }
            })
            //silent	SilentMode	是否使用静默恢复，如果设置为true的情况下，那么在发生Crash时将不显示RecoveryActivity界面来进行恢复，而是自动的恢复Activity的堆栈和数据，也就是无界面恢复
            .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
            .init(applicationContext)
//        //LeakCanary 会增加应用的内存和性能开销
//        // 创建 LeakCanary 配置
//        val config = LeakCanary.Config(
//            dumpHeap = true, // 是否在检测到内存泄漏时转储堆
//            retainedVisibleThreshold = 5// 保留对象的可见阈值
//        )
//        // 应用配置
//        LeakCanary.config = config
//        // 启动 LeakCanary 显示 LeakCanary 图标
//        LeakCanary.showLeakDisplayActivityLauncherIcon(true)
    }

    private fun initAlbum() {
        Album.initialize(AlbumConfig.newBuilder(applicationContext)
            .setAlbumLoader(GlideLoader()) //设置Album加载器。
            .setLocale(Locale.CHINA) //强制设置在任何语言下都用中文显示。
            .build())
    }

    private fun initDao() {
        //确保只初始化一次（Kotlin内部处理线程安全）
        if (!::daoMaster.isInitialized) {
            try {
                val dbOpenHelper = DaoMaster.DevOpenHelper(applicationContext, "${VERSION_NAME}.db", null)
                val readableDb = dbOpenHelper.readableDb
                daoMaster = DaoMaster(readableDb)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!::boxStore.isInitialized) {
            boxStore = MyObjectBox.builder()
                .androidContext(applicationContext)
                .build()
        }
    }

    private fun initOssDao() {
        OssDBHelper.init(daoMaster.newSession().ossDBDao)
        OssDBHelper2.init(boxStore)
    }

    private fun initOss() {
        OssFactory.instance.initialize()
    }

    private fun initAMap() {
        //高德地图隐私政策合规
        ServiceSettings.updatePrivacyShow(applicationContext, true, true)
        ServiceSettings.updatePrivacyAgree(applicationContext, true)
    }

//    /**
//     * 程序被销毁时会调用，真机不会调取
//     */
//    override fun onTerminate() {
//        super.onTerminate()
//        WXManager.instance.unRegToWx()
//    }

}