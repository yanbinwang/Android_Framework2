package com.example.mvvm

import android.content.Context
import android.os.Looper
import android.util.Log
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

/**
 * Created by WangYanBin on 2020/8/14.
 */
class MyApplication : BaseApplication() {

    companion object {
        val instance: MyApplication
            get() = BaseApplication.instance as MyApplication
        //my中的三方库是否完成加载
        var isLoaded = false
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        //初始化系统通知
        initNotification()
        //不同包体初始化不同类
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
        try {
            //初始化firebase->没有谷歌服务的手机会报错
            initFireBase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        //初始化图片库类
//        initAlbum()
//        //数据库初始化
//        initOssDao()
//        //初始化oss
//        initOss()
//        //初始化进程监听
//        setOnStateChangedListener {
//            if (it) {
//                initOss()
//            }
//        }
//        //授权初始化
//        setOnPrivacyAgreedListener {
//            if (it && !isLoaded) {
//                isLoaded = true
//                initAMap()
//            }
//        }
//        //初始化需要授权的库->重写是为了触发setOnPrivacyAgreedListener，传false的话BaseApplication内的就不会再初始化一次了
//        initPrivacyAgreed(false)
    }

    private fun initDebugging() {
        try {
            // 加载 DebuggingUtil 类
            val debuggingUtilClass = Class.forName("com.example.debugging.utils.DebuggingUtil")
            // 获取 init 方法（参数：Context, Class<?>）
            val initMethod = debuggingUtilClass.getMethod("init", Context::class.java, Class::class.java)
            // 调用静态方法
            initMethod.invoke(null, applicationContext, MainActivity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
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