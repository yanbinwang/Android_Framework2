package com.example.common

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.os.SystemClock
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.BaseActivity
import com.example.common.base.OnFinishListener
import com.example.common.base.proxy.ApplicationActivityLifecycleCallbacks
import com.example.common.base.proxy.NetworkCallbackImpl
import com.example.common.base.proxy.NetworkReceiver
import com.example.common.config.ARouterPath
import com.example.common.config.Constants.SOCKET_ADVERTISE_URL
import com.example.common.config.Constants.SOCKET_DEAL_URL
import com.example.common.config.Constants.SOCKET_FUNDS_URL
import com.example.common.config.ServerConfig
import com.example.common.event.EventCode.EVENT_OFFLINE
import com.example.common.event.EventCode.EVENT_ONLINE
import com.example.common.socket.SocketEventCode.EVENT_SOCKET_ADVERTISE
import com.example.common.socket.SocketEventCode.EVENT_SOCKET_DEAL
import com.example.common.socket.SocketEventCode.EVENT_SOCKET_FUNDS
import com.example.common.socket.topic.WebSocketTopic
import com.example.common.utils.manager.AppManager
import com.example.common.utils.builder.ToastBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.helper.ConfigHelper
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshFooter
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.minute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.glide.ImageLoader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.util.Locale

/**
 * Created by WangYanBin on 2020/8/14.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("MissingPermission", "UnspecifiedRegisterReceiverFlag", "PrivateApi", "DiscouragedPrivateApi", "SoonBlockedPrivateApi", "Build.VERSION_CODES.ICE_CREAM_SANDWICH")
abstract class BaseApplication : Application() {
    private var onStateChangedListener: (isForeground: Boolean) -> Unit = {}
    private var onPrivacyAgreedListener: (isAgreed: Boolean) -> Unit = {}

    companion object {
        //当前app进程是否处于前台
        var isForeground = true
        //是否需要回首頁
        var needOpenHome = false
        //应用启动时间戳
        var startTime = -1L
        //单列
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        /**
         * 存储启动时间戳
         * 返回的是自系统启动开始到调用该方法时所经过的时间，包含了系统处于睡眠状态的时间。也就是说，从设备开机（包括关机充电等情况）起，
         * 不管设备是处于正常运行、休眠还是其他状态，这个时间都会持续累加。该方法返回的时间单位是毫秒（ms）
         * // 获取启动时间戳
         * long startTime = MyApplication.getStartTime();
         * // 计算已经过去的时间
         * long elapsedTime = SystemClock.elapsedRealtime() - startTime;
         * // 计算还需要等待的时间
         * long remainingTime = SPLASH_DELAY - elapsedTime;
         * if (remainingTime < 0) {
         *    remainingTime = 0;
         *  }
         */
        startTime = SystemClock.elapsedRealtime()
        //布局初始化
        AutoSizeConfig.getInstance()
            .setBaseOnWidth(true)
            .unitsManager
            .setSupportDP(false)
            .setSupportSP(false)
            .supportSubunits = Subunits.PT
        //腾讯读写mmkv初始化
        MMKV.initialize(this)
        //服务器地址类初始化
        ServerConfig.init()
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(ApplicationActivityLifecycleCallbacks())
        //解决androidP 第一次打开程序出现莫名弹窗-弹窗内容“detected problems with api ”
        closeAndroidPDialog()
        //阿里路由跳转初始化
        initARouter()
        //注册网络监听
        initReceiver()
        //部分推送打開的頁面，需要在關閉時回首頁,實現一個透明的activity，跳轉到對應push的activity之前，讓needOpenHome=true
        initListener()
        //全局刷新控件的样式
        initSmartRefresh()
        //全局toast
        initToast()
        //初始化socket
        initSocket()
        //全局进程
        initLifecycle()
        //初始化友盟/人脸识别->延后
        initPrivacyAgreed()
    }

    private fun closeAndroidPDialog() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            try {
                val aClass = Class.forName("android.content.pm.PackageParser\$Package")
                val declaredConstructor = aClass.getDeclaredConstructor(String::class.java)
                declaredConstructor.setAccessible(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val cls = Class.forName("android.app.ActivityThread")
                val declaredMethod = cls.getDeclaredMethod("currentActivityThread")
                declaredMethod.isAccessible = true
                val activityThread = declaredMethod.invoke(null)
                val mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown")
                mHiddenApiWarningShown.isAccessible = true
                mHiddenApiWarningShown.setBoolean(activityThread, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initARouter() {
        //开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        if (isDebug) {
            ARouter.openLog()//打印日志
            ARouter.openDebug()
        }
        ARouter.init(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initReceiver() {
        (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.registerNetworkCallback(NetworkRequest.Builder().build(), NetworkCallbackImpl())
        registerReceiver(NetworkReceiver().apply {
            listener = { if (it) EVENT_ONLINE.post() else EVENT_OFFLINE.post() }
        }, NetworkReceiver.filter)
    }

    private fun initListener() {
        BaseActivity.onFinishListener = object : OnFinishListener {
            override fun onFinish(act: BaseActivity<*>) {
                if (!needOpenHome) return
                if (BaseActivity.isAnyActivityStarting) return
                val clazzName = act.javaClass.simpleName.lowercase(Locale.getDefault())
                if (clazzName == "mainactivity") return
                if (clazzName == "splashactivity") return
                if (AppManager.currentActivity() != act) return
                if (AppManager.stackCount <= 1) {
                    needOpenHome = false
                    ARouter.getInstance().build(ARouterPath.MainActivity).navigation()
                }
            }
        }
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
        ToastBuilder.setResToastBuilder { message, length ->
            val toast = Toast(instance)
            //设置Toast要显示的位置，居中，X轴偏移0个单位，Y轴偏移0个单位，
            toast.setGravity(Gravity.CENTER, 0, 0)
            //设置显示时间
            toast.duration = length
            val view = TextView(instance)
            view.text = string(message)
            view.setBackgroundResource(R.drawable.shape_toast)
//            view.background = drawable
            view.minHeight = 40.pt
            view.minWidth = 190.pt
            view.padding(start = 20.pt, end = 20.pt, top = 5.pt, bottom = 5.pt)
            view.gravity = Gravity.CENTER
            view.textSize(R.dimen.textSize14)
            view.textColor(R.color.textWhite)
            toast.view = view
            return@setResToastBuilder toast
        }
        ToastBuilder.setTextToastBuilder { message, length ->
            val toast = Toast(instance)
            //设置Toast要显示的位置，居中，X轴偏移0个单位，Y轴偏移0个单位，
            toast.setGravity(Gravity.CENTER, 0, 0)
            //设置显示时间
            toast.duration = length
            val view = TextView(instance)
            view.text = message
            view.setBackgroundResource(R.drawable.shape_toast)
            view.minHeight = 40.pt
            view.minWidth = 190.pt
            view.padding(start = 20.pt, end = 20.pt, top = 5.pt, bottom = 5.pt)
            view.gravity = Gravity.CENTER
            view.textSize(R.dimen.textSize14)
            view.textColor(R.color.textWhite)
            toast.view = view
            return@setTextToastBuilder toast
        }
    }

    private fun initSocket() {
        WebSocketTopic.setOnMessageListener { url, data ->
            val payload = data?.payload.orEmpty()
            when (url) {
                SOCKET_DEAL_URL -> EVENT_SOCKET_DEAL.post(payload)
                SOCKET_ADVERTISE_URL -> EVENT_SOCKET_ADVERTISE.post(payload)
                SOCKET_FUNDS_URL -> EVENT_SOCKET_FUNDS.post(payload)
            }
        }
    }

    /**
     * 监听切换到前台，超过5分钟部分第三方重新获取
     */
    private fun initLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            private var isFirst = true
            private var timeStamp = System.currentTimeMillis()
            private var timeNano = System.nanoTime()

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        isForeground = true
//                        EventCode.EVENT_FOREGROUND.post()
                        if (isFirst) {
                            isFirst = false
                        } else {
                            val stampTimeDiff = System.currentTimeMillis() - timeStamp
                            val nanoTimeDiff = (System.nanoTime() - timeNano) / 1000000L
                            //此处多个第三方可重新初始化(超过120分钟就重新初始化，避免过期)
                            if (stampTimeDiff - nanoTimeDiff > 120.minute) {
                                onStateChangedListener.invoke(true)
                            }
                            timeStamp = System.currentTimeMillis()
                            timeNano = System.nanoTime()
                        }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        //判断本程序process中是否有在任意前台
                        val isAnyProcessForeground = try {
                            (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses?.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
                        } catch (e: Exception) {
                            false
                        }
                        if (!isAnyProcessForeground.orFalse) {
                            isForeground = false
//                            EventCode.EVENT_BACKGROUND.post()
                            onStateChangedListener.invoke(false)
                            timeStamp = System.currentTimeMillis()
                            timeNano = System.nanoTime()
                        }
                    }
                    else -> {}
                }
            }
        })
    }

    protected fun setOnStateChangedListener(onStateChangedListener: (isForeground: Boolean) -> Unit) {
        this.onStateChangedListener = onStateChangedListener
    }

    protected fun setOnPrivacyAgreedListener(onPrivacyAgreedListener: (agreed: Boolean) -> Unit) {
        this.onPrivacyAgreedListener = onPrivacyAgreedListener
    }

    fun initPrivacyAgreed(isInitial: Boolean = true) {
        if (ConfigHelper.getPrivacyAgreed()) {
            if (isInitial) {
//            //友盟日志收集
//            initUM()
//            //支付宝人脸识别
//            initVerify()
            }
            onPrivacyAgreedListener.invoke(true)
        } else {
            onPrivacyAgreedListener.invoke(false)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        System.gc()
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            ImageLoader.instance.clearMemoryCache(this, ProcessLifecycleOwner.get())
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.instance.clearMemoryCache(this, ProcessLifecycleOwner.get())
    }

}