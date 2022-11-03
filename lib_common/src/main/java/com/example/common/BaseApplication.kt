package com.example.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.proxy.ApplicationActivityLifecycleCallbacks
import com.example.common.base.proxy.NetworkCallbackImpl
import com.example.common.imageloader.album.AlbumGlideLoader
import com.example.common.utils.helper.ConfigHelper
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshFooter
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import com.tencent.smtt.sdk.QbSdk
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.util.*

/**
 * Created by WangYanBin on 2020/8/14.
 */
@SuppressLint("MissingPermission")
open class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        //布局初始化
        AutoSizeConfig.getInstance()
            .unitsManager
            .setSupportDP(false)
            .setSupportSP(false)
            .supportSubunits = Subunits.MM
        //初始化图片库类
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(AlbumGlideLoader()) //设置Album加载器。
                .setLocale(Locale.CHINA) //强制设置在任何语言下都用中文显示。
                .build())
        //x5内核初始化接口
        QbSdk.initX5Environment(applicationContext, null)
        //阿里路由跳转初始化
        if (BuildConfig.DEBUG) {
            ARouter.openLog()//打印日志
            ARouter.openDebug()
        }
        //开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.init(this)
        //腾讯读写mmkv初始化
        MMKV.initialize(this)
        //基础配置初始化
        ConfigHelper.initialize(this)
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(ApplicationActivityLifecycleCallbacks())
        //注册网络监听
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerNetworkCallback(NetworkRequest.Builder().build(), NetworkCallbackImpl())
        //全局刷新控件的样式
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            //全局设置主题颜色
            layout.setPrimaryColorsId(R.color.grey_f6f8ff, R.color.white_00ffffff)
            ProjectRefreshHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ -> ProjectRefreshFooter(context) }
    }

}