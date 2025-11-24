package com.example.common

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.SystemClock
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.common.base.BaseActivity
import com.example.common.base.OnFinishListener
import com.example.common.base.page.PageInterceptor
import com.example.common.base.proxy.ApplicationActivityLifecycleCallbacks
import com.example.common.config.Constants.SOCKET_ADVERTISE_URL
import com.example.common.config.Constants.SOCKET_DEAL_URL
import com.example.common.config.Constants.SOCKET_FUNDS_URL
import com.example.common.config.RouterPath
import com.example.common.config.ServerConfig
import com.example.common.network.socket.SocketEventCode.EVENT_SOCKET_ADVERTISE
import com.example.common.network.socket.SocketEventCode.EVENT_SOCKET_DEAL
import com.example.common.network.socket.SocketEventCode.EVENT_SOCKET_FUNDS
import com.example.common.network.socket.topic.WebSocketTopic
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.ToastBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.i18n.I18nUtil.getPackVersion
import com.example.common.utils.i18n.LanguageUtil.checkLanguageVersion
import com.example.common.utils.i18n.LanguageUtil.resetLanguage
import com.example.common.utils.i18n.LanguageUtil.setLocalLanguage
import com.example.common.utils.manager.AppManager
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshFooter
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.DateFormat.clearThreadLocalCache
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.minute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.glide.ImageLoader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import com.therouter.TheRouter
import com.therouter.router.setRouterInterceptor
import com.therouter.theRouterInited
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by WangYanBin on 2020/8/14.
 */
@SuppressLint("MissingPermission", "UnspecifiedRegisterReceiverFlag", "PrivateApi", "DiscouragedPrivateApi", "SoonBlockedPrivateApi")
abstract class BaseApplication : Application() {
    private var onStateChangedListener: (isForeground: Boolean) -> Unit = {}
    private var onPrivacyAgreedListener: (isAgreed: Boolean) -> Unit = {}
    private val excludedRouterPaths by lazy {
        listOf(
            RouterPath.MainActivity,
            RouterPath.SplashActivity,
//            RouterPath.LinkActivity,
//            RouterPath.LinkHandlerActivity
        ).map { it.replace("/app/", "").lowercase(Locale.getDefault()) }.toSet()
    }

    companion object {
        // 单列
        lateinit var instance: BaseApplication
        // 是否需要回首頁->只有推送LinkActivity拉起的时候会为true，并且会在首页变回false
        var needOpenHome = AtomicBoolean(false)
        // 当前app进程是否处于前台
        var isForeground = AtomicBoolean(true)
        // 首次启动标记（仅在 onCreate 初始化）
        var isFirstLaunch = AtomicBoolean(true)
        // 最近一次点击图标启动的时间戳
        var lastClickTime = AtomicLong(0L)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initialize()
    }

    // 初始化一些第三方控件和单例工具类等
    private fun initialize() {
        // 初次赋值
        lastClickTime.set(SystemClock.elapsedRealtime())
        isFirstLaunch.set(true)
        // 布局初始化
        AutoSizeConfig.getInstance()
            .setBaseOnWidth(true)
            .unitsManager
            .setSupportDP(false)
            .setSupportSP(false)
            .supportSubunits = Subunits.PT
        // 腾讯读写mmkv初始化
        MMKV.initialize(applicationContext)
        // 服务器地址类初始化
        ServerConfig.init()
        // 注册网络监听
        NetWorkUtil.init(ProcessLifecycleOwner.get())
        // 防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(ApplicationActivityLifecycleCallbacks())
        // 語言包初始化
        initLanguage()
//        // 解决androidP 第一次打开程序出现莫名弹窗-弹窗内容“detected problems with api ”
//        closeAndroidPDialog()
        // 路由跳转初始化
        initRouter()
        // 部分推送打開的頁面，需要在關閉時回首頁,實現一個透明的activity，跳轉到對應push的activity之前，讓needOpenHome=true
        initListener()
        // 全局刷新控件的样式
        initSmartRefresh()
        // 全局toast
        initToast()
        // 初始化socket
        initSocket()
        // 全局进程
        initLifecycle()
//        // 初始化友盟/人脸识别->延后
//        initPrivacyAgreed()
    }

    private fun initRouter() {
        // 手动初始化 TheRouter
        if (!theRouterInited()) {
            TheRouter.init(this)
        }
        // 设置 debug 模式
        TheRouter.isDebug = isDebug
        // 设置全局AOP拦截器 将 PageInterceptor 设置为全局唯一的路由拦截器
        setRouterInterceptor(PageInterceptor())
    }

    /**
     * 默认如果没有存储服务器的bean会走本地的assets下配置的语言包
     * 如果用户进入设置，并选择了对应的语言，则会存储用户选择的语言的bean，并在应用启动时应用存储下来的bean
     * 1.获取支持的语种列表
     * 2.点击对应语种列表通过其url再请求获取bean对象，并存储替换本地的bean
     */
    private fun initLanguage() {
        if (getPackVersion() <= 0) {
            // 语言包未配置
            resetLanguage()
            setLocalLanguage()
        } else {
            // 语言包已配置
            checkLanguageVersion()
        }
    }

    private fun initListener() {
        // 所有继承了BaseActivity的页面在应用进程内都有关闭监听
        BaseActivity.onFinishListener = object : OnFinishListener {
            override fun onFinish(act: BaseActivity<*>) {
                // 是否需要打开首页(任务栈最底层显示的页面 -> Main/Home)
                if (!needOpenHome.get()) return
                // 推送/模块内使用startActivity/startActivityForResult扩展函数调用的页面处于只能在开启状态时不会被调用
                if (BaseActivity.isAnyActivityStarting) return
                // 获取关闭的页面的class名,校验是否处于排除规则内,如果是则不会被调用
                val clazzName = act.javaClass.simpleName.lowercase(Locale.getDefault())
                if (excludedRouterPaths.contains(clazzName)) return
                // 判断当前选中位于最前端的用户页面是否是关闭的页面,以及当前任务栈内是否只存在一个页面
                if (AppManager.currentActivity() != act) return
                if (AppManager.dequeCount <= 1) {
                    // 拉起首页(配置了singleTask,栈内不会重复)
                    needOpenHome.set(false)
                    TheRouter.build(RouterPath.MainActivity).navigation()
                }
            }
        }
    }

    private fun initSmartRefresh() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            // 全局设置主题颜色
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
            // 设置Toast要显示的位置，居中，X轴偏移0个单位，Y轴偏移0个单位，
            toast.setGravity(Gravity.CENTER, 0, 0)
            // 设置显示时间
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
            toast.setGravity(Gravity.CENTER, 0, 0)
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
                        isForeground.set(true)
//                        EventCode.EVENT_FOREGROUND.post()
                        if (isFirst) {
                            isFirst = false
                        } else {
                            val stampTimeDiff = System.currentTimeMillis() - timeStamp
                            val nanoTimeDiff = (System.nanoTime() - timeNano) / 1000000L
                            // 此处多个第三方可重新初始化(超过120分钟就重新初始化，避免过期)
                            if (stampTimeDiff - nanoTimeDiff > 120.minute) {
                                onStateChangedListener.invoke(true)
                            }
                            timeStamp = System.currentTimeMillis()
                            timeNano = System.nanoTime()
                        }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        // 判断本程序process中是否有在任意前台
                        val isAnyProcessForeground = try {
                            (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses?.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            false
                        }
                        if (!isAnyProcessForeground.orFalse) {
                            isForeground.set(false)
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

//    protected fun setOnStateChangedListener(onStateChangedListener: (isForeground: Boolean) -> Unit) {
//        this.onStateChangedListener = onStateChangedListener
//    }
//
//    protected fun setOnPrivacyAgreedListener(onPrivacyAgreedListener: (agreed: Boolean) -> Unit) {
//        this.onPrivacyAgreedListener = onPrivacyAgreedListener
//    }
//
//    fun initPrivacyAgreed(isBaseLoaded: Boolean = true) {
//        if (ConfigHelper.getPrivacyAgreed()) {
//            if (isBaseLoaded) {
////            //友盟日志收集
////            initUM()
////            //支付宝人脸识别
////            initVerify()
//            }
//            onPrivacyAgreedListener.invoke(true)
//        } else {
//            onPrivacyAgreedListener.invoke(false)
//        }
//    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        System.gc()
        /**
         * TRIM_MEMORY_MODERATE: 应用处于 “后台 LRU 列表的中间位置”——
         * 此时系统内存已较紧张，主动释放内存（如缓存、非关键图片等），能帮助系统保留 LRU 列表中更靠后的其他进程，提升整体系统性能。
         * 从 API 34（Android 14）开始废弃,Android 14 后，系统对后台进程的管理更精细化（如基于进程重要性、用户活跃度动态调整内存优先级），
         * 不再需要通过 “中间级别” 通知应用。系统会直接通过更关键的级别（如 TRIM_MEMORY_RUNNING_LOW、TRIM_MEMORY_BACKGROUND）传递核心内存压力信号，避免应用接收冗余或模糊的指令。
         * TRIM_MEMORY_BACKGROUND（40）的语义是 “应用在后台 LRU 列表靠前位置，系统开始回收后台进程”，与 TRIM_MEMORY_MODERATE（60）的 “后台中间位置” 在实际场景中差异不大，都是需要释放缓存的信号。
         */
        val shouldClearCache = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+：判断是否达到后台内存紧张级别
            level >= TRIM_MEMORY_BACKGROUND
        } else {
            // 低版本：保留原逻辑（TRIM_MEMORY_MODERATE仍有效）
            level >= TRIM_MEMORY_MODERATE
        }
        if (shouldClearCache) {
            ImageLoader.instance.clearMemoryCache(applicationContext, ProcessLifecycleOwner.get())
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.instance.clearMemoryCache(applicationContext, ProcessLifecycleOwner.get())
    }

    override fun onTerminate() {
        super.onTerminate()
        clearThreadLocalCache()
    }

}