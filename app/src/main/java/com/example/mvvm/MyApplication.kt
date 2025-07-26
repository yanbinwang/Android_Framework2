package com.example.mvvm

import android.content.Context
import android.os.Looper
import android.util.Log
import com.amap.api.services.core.ServiceSettings
import com.example.common.BaseApplication
import com.example.common.config.Constants.VERSION_NAME
import com.example.framework.utils.function.value.isDebug
import com.example.gallery.GlideLoader
import com.example.greendao.dao.DaoMaster
import com.example.mvvm.activity.MainActivity
import com.example.objectbox.dao.MyObjectBox
import com.example.thirdparty.media.oss.OssDBHelper
import com.example.thirdparty.media.oss.OssDBHelper2
import com.example.thirdparty.media.oss.OssFactory
import com.example.thirdparty.utils.NotificationUtil
import com.example.thirdparty.utils.wechat.WXManager
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
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
        //my中的三方库是否完成加载
        var isLoaded = false
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
        //通知栏初始化
        initNotification()
        //初始化图片库类
        initAlbum()
        //数据库初始化
        initDao()
        initOssDao()
        //初始化oss
        initOss()
        //初始化进程监听
        setOnStateChangedListener {
            if (it) {
                initOss()
            }
        }
        //授权初始化
        setOnPrivacyAgreedListener {
            if (it && !isLoaded) {
                isLoaded = true
                initAMap()
            }
        }
        //初始化需要授权的库->重写是为了触发setOnPrivacyAgreedListener，传false的话BaseApplication内的就不会再初始化一次了
        initPrivacyAgreed(false)
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

    private fun initNotification() {
        NotificationUtil.init(applicationContext)
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

    /**
     * 程序被销毁时会调用，真机不会调取
     */
    override fun onTerminate() {
        super.onTerminate()
        isLoaded = false
        boxStore.close()
        WXManager.instance.unRegToWx()
    }

}