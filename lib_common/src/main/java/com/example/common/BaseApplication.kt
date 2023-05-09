package com.example.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.BaseActivity
import com.example.common.base.OnFinishListener
import com.example.common.base.proxy.ApplicationActivityLifecycleCallbacks
import com.example.common.base.proxy.NetworkCallbackImpl
import com.example.common.base.proxy.NetworkReceiver
import com.example.common.config.ARouterPath
import com.example.common.config.ServerConfig
import com.example.common.event.EventCode.EVENT_OFFLINE
import com.example.common.event.EventCode.EVENT_ONLINE
import com.example.common.utils.AppManager
import com.example.common.utils.builder.ToastBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.common.utils.helper.ConfigHelper
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshFooter
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.glide.ImageLoader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.util.*

/**
 * Created by WangYanBin on 2020/8/14.
 */
@SuppressLint("MissingPermission")
abstract class BaseApplication : Application() {

    companion object {
        //是否需要回首頁
        var needOpenHome = false
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
            .setBaseOnWidth(true)
            .unitsManager
            .setSupportDP(false)
            .setSupportSP(false)
            .supportSubunits = Subunits.PT
        //阿里路由跳转初始化
        initARouter()
        //腾讯读写mmkv初始化
        MMKV.initialize(this)
        //基础配置初始化
        ConfigHelper.initialize(this)
        //服务器地址类初始化
        ServerConfig.init()
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(ApplicationActivityLifecycleCallbacks())
        //注册网络监听
        initReceiver()
        //部分推送打開的頁面，需要在關閉時回首頁,實現一個透明的activity，跳轉到對應push的activity之前，讓needOpenHome=true
        initListener()
        //全局刷新控件的样式
        initSmartRefresh()
        //全局toast
        initToast()
    }

    private fun initARouter() {
        if (isDebug) {
            ARouter.openLog()//打印日志
            ARouter.openDebug()
        }
        //开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.init(this)
    }

    private fun initReceiver() {
        (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.registerNetworkCallback(NetworkRequest.Builder().build(), NetworkCallbackImpl())
        registerReceiver(NetworkReceiver().apply {
            listener = { if (it) EVENT_ONLINE.post() else EVENT_OFFLINE.post() }
        }, NetworkReceiver.filter)
    }

    private fun initListener() {
        BaseActivity.setOnFinishListener(object : OnFinishListener {
            override fun onFinish(act: BaseActivity<*>) {
                if (!needOpenHome) return
                if (act.TAG == "HomeActivity") return
                if (act.TAG == "SplashActivity") return
                if (AppManager.currentActivity() != act) return
                if (AppManager.stackCount <= 1) {
                    needOpenHome = false
                    ARouter.getInstance().build(ARouterPath.MainActivity).navigation()
                }
            }
        })
    }

    private fun initSmartRefresh() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            //全局设置主题颜色
//            layout.setPrimaryColorsId(R.color.grey_f6f8ff, R.color.white_00ffffff)
            ProjectRefreshHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ProjectRefreshFooter(context)
        }
    }

    private fun initToast() {
        val drawable = GradientDrawable().apply {
            setColor(Color.parseColor("#4c000000"))
            cornerRadius = 7.ptFloat
        }
        ToastBuilder.setResToastBuilder { message, length ->
            val toast = Toast(instance)
            //设置Toast要显示的位置，居中，X轴偏移0个单位，Y轴偏移0个单位，
            toast.setGravity(Gravity.CENTER, 0, 0)
            //设置显示时间
            toast.duration = length
            val view = TextView(instance)
            view.text = string(message)
//            view.setBackgroundResource(R.drawable.shape_toast_bg)
            view.background = drawable
            view.minHeight = 40.pt
            view.minWidth = 190.pt
            view.padding(start = 20.pt, end = 20.pt, top = 5.pt, bottom = 5.pt)
            view.gravity = Gravity.CENTER
            view.textSize(R.dimen.textSize14)
            view.textColor(R.color.white)
            toast.view = view
            return@setResToastBuilder toast
        }
        ToastBuilder.setStringToastBuilder { message, length ->
            val toast = Toast(instance)
            //设置Toast要显示的位置，居中，X轴偏移0个单位，Y轴偏移0个单位，
            toast.setGravity(Gravity.CENTER, 0, 0)
            //设置显示时间
            toast.duration = length
            val view = TextView(instance)
            view.text = message
            view.background = drawable
            view.minHeight = 40.pt
            view.minWidth = 190.pt
            view.padding(start = 20.pt, end = 20.pt, top = 5.pt, bottom = 5.pt)
            view.gravity = Gravity.CENTER
            view.textSize(R.dimen.textSize14)
            view.textColor(R.color.white)
            toast.view = view
            return@setStringToastBuilder toast
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        System.gc()
        try {
            if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
                ImageLoader.instance.clearMemoryCache(this)
            }
        } catch (ignore: Exception) {
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        try {
            ImageLoader.instance.clearMemoryCache(this)
        } catch (ignore: Exception) {
        }
    }

}