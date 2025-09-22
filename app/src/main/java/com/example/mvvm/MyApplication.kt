package com.example.mvvm

import android.content.Context
import com.example.common.BaseApplication
import com.example.common.utils.helper.ConfigHelper
import com.example.common.utils.toJson
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toArray
import com.example.framework.utils.logE
import com.example.framework.utils.logWTF
import com.example.home.activity.LinkActivity
import com.example.mvvm.activity.MainActivity
import com.example.thirdparty.firebase.utils.FireBaseUtil
import com.example.thirdparty.utils.NotificationUtil
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Created by WangYanBin on 2020/8/14.
 */
class MyApplication : BaseApplication() {

    companion object {
        val instance: MyApplication
            get() = BaseApplication.instance as MyApplication
        // my中的三方库是否完成加载
        var isLoaded = AtomicBoolean(false)
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    // 初始化一些第三方控件和单例工具类等
    private fun initialize() {
        // 初始化系统通知
        initNotification()
        // 不同包体初始化不同类
        if (isDebug) {
            initDebugging()
        } else {
            initCrashHandler()
        }
        try {
            // 初始化firebase->没有谷歌服务的手机会报错
            initFireBase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        // 初始化图片库类
//        initAlbum()
//        // 数据库初始化
//        initOssDao()
//        // 初始化oss
//        initOss()
//        // 初始化进程监听
//        setOnStateChangedListener {
//            if (it) {
//                initOss()
//            }
//        }
//        // 授权初始化
//        setOnPrivacyAgreedListener {
//            if (it && !isLoaded.get()) {
//                isLoaded.set(true)
//                initAMap()
//            }
//        }
//        // 初始化需要授权的库->重写是为了触发setOnPrivacyAgreedListener，传false的话BaseApplication内的就不会再初始化一次了
//        initPrivacyAgreed(false)
    }

    /**
     * 1. getMethod(String name, Class<?>... parameterTypes)
     * 作用：获取当前类或其父类中所有 public 方法，包括继承的方法。
     * 若方法存在但非 public，会抛出 NoSuchMethodException。
     * 只能获取 public 方法，无法获取 private/protected 方法。
     * 2. getDeclaredMethod(String name, Class<?>... parameterTypes)
     * 作用：获取当前类中所有声明的方法（无论访问修饰符），但不包括父类的方法。
     * 虽然能获取私有方法，但调用前需通过 method.setAccessible(true) 突破访问限制。
     * 无法获取父类的方法（即使父类方法是 public）。
     */
    private fun initDebugging() {
        try {
            // 加载 DebuggingUtil 类
            val debuggingUtilClass = Class.forName("com.example.debugging.utils.DebuggingUtil")
            // 获取 init 方法（参数：Context, Class<?>）
            val initMethod = debuggingUtilClass.getMethod("init", Context::class.java, Class::class.java)
//            // 解除访问限制（如果是private）
//            initMethod.isAccessible = true
            // 调用静态方法
            initMethod.invoke(null, applicationContext, MainActivity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initCrashHandler() {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
//            // 1. 捕获异常并生成日志
//            val crashLog = generateCrashLog(throwable, thread.let { it.name to it.id })
//            // 2. 保存日志到本地文件
//            saveCrashLogToFile(crashLog)
            // 3.正常退出，不强行重启
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
    }

    private fun initNotification() {
        NotificationUtil.init(applicationContext)
    }

    private fun initFireBase() {
        FireBaseUtil.initialize(applicationContext)
        FireBaseUtil.notificationIntentGenerator = { _, map ->
            " \n收到firebase\nmap:${map.toJson()}".logWTF
            LinkActivity.byPush(instance, *map.toArray { it.key to it.value })
        }
        FireBaseUtil.tokenRefreshListener = {
            "firebase token $it".logE
            ConfigHelper.setDeviceToken(it)
        }
    }

//    private fun initAlbum() {
//        Album.initialize(AlbumConfig.newBuilder(applicationContext)
//            .setAlbumLoader(GlideLoader()) //设置Album加载器。
//            .setLocale(Locale.CHINA) //强制设置在任何语言下都用中文显示。
//            .build())
//    }
//
//    private fun initOssDao() {
//        OssHelper.init(DaoMaster(DaoMaster.DevOpenHelper(applicationContext, "${VERSION_NAME}.db", null).readableDb).newSession().ossDBDao)
//    }
//
//    private fun initOss() {
//        OssFactory.instance.initialize()
//    }
//
//    private fun initAMap() {
//        //高德地图隐私政策合规
//        ServiceSettings.updatePrivacyShow(applicationContext, true, true)
//        ServiceSettings.updatePrivacyAgree(applicationContext, true)
//    }

}