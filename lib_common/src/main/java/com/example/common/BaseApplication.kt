package com.example.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.constant.Constants
import com.example.common.imageloader.glide.callback.GlideAlbumLoader
import com.example.framework.utils.LogUtil.d
import com.example.framework.utils.LogUtil.e
import com.tencent.mmkv.MMKV
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.QbSdk.PreInitCallback
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Created by WangYanBin on 2020/7/10.
 */
@SuppressLint("MissingPermission")
open class BaseApplication : Application() {

    companion object {
        val instance: BaseApplication by lazy {
            BaseApplication()
        }
    }

    override fun onCreate() {
        super.onCreate()
        //初始化配置
        initialize()
    }

    // <editor-fold defaultstate="collapsed" desc="获取默认参数和配置">
    private fun initialize() {
        //布局初始化
        AutoSizeConfig.getInstance().unitsManager
            .setSupportDP(false)
            .setSupportSP(false).supportSubunits = Subunits.MM
        //初始化图片库类
        Album.initialize(
            AlbumConfig.newBuilder(this)
                .setAlbumLoader(GlideAlbumLoader()) //设置Album加载器。
                .setLocale(Locale.CHINA) //强制设置在任何语言下都用中文显示。
                .build()
        )
        //x5内核初始化接口
        QbSdk.initX5Environment(applicationContext, object : PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                d(" onViewInitFinished is $arg0")
            }

            override fun onCoreInitFinished() {}
        })
        //阿里路由跳转初始化
        if (BuildConfig.DEBUG) {
            ARouter.openLog() // 打印日志
            ARouter.openDebug()
        }
        // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.init(this)
        //腾讯读写mmkv初始化
        MMKV.initialize(this)
        //在程序运行时取值，保证长宽静态变量不丢失
        val metric = DisplayMetrics()
        val mWindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.defaultDisplay.getMetrics(metric)
        //屏幕宽度（像素）
        Constants.SCREEN_WIDTH = metric.widthPixels
        //屏幕高度（像素）
        Constants.SCREEN_HEIGHT = metric.heightPixels
        //获取手机的导航栏高度
        Constants.STATUS_BAR_HEIGHT = resources.getDimensionPixelSize(
            resources.getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
            )
        )
        //获取手机的网络ip
        Constants.IP = getIp()
        //获取手机的Mac地址
        Constants.MAC = getMac()
        //获取手机的DeviceId
        Constants.DEVICE_ID = getDeviceId()
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activity!!.window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
                    proxyOnClick(activity.window.decorView, 5)
                }
            }

            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }
        })
    }

    private fun getIp(): String? {
        val info =
            (applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (info != null && info.isConnected) {
            if (info.type == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
                try {
                    val en = NetworkInterface.getNetworkInterfaces()
                    while (en.hasMoreElements()) {
                        val intf = en.nextElement()
                        val enumIpAddr = intf.inetAddresses
                        while (enumIpAddr.hasMoreElements()) {
                            val inetAddress = enumIpAddr.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress.getHostAddress()
                            }
                        }
                    }
                } catch (e: SocketException) {
                    return null
                }
            } else if (info.type == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
                val wifiManager =
                    applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                //得到IPV4地址
                return initIp(wifiInfo.ipAddress)
            }
        }
        return null
    }

    private fun initIp(ipInt: Int): String? {
        return (ipInt and 0xFF).toString() + "." +
                (ipInt shr 8 and 0xFF) + "." +
                (ipInt shr 16 and 0xFF) + "." +
                (ipInt shr 24 and 0xFF)
    }

    private fun getMac(): String? {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return null
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return null
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String? {
        return try {
            (applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
        } catch (e: SecurityException) {
            null
        }
    }

    private fun proxyOnClick(view: View, recycledContainerDeep: Int) {
        var recycledContainerDeep = recycledContainerDeep
        if (view.visibility == View.VISIBLE) {
            if (view is ViewGroup) {
                val existAncestorRecycle = recycledContainerDeep > 0
                val p = view
                if (p !is AbsListView || existAncestorRecycle) {
                    getClickListenerForView(view)
                    if (existAncestorRecycle) {
                        recycledContainerDeep++
                    }
                } else {
                    recycledContainerDeep = 1
                }
                val childCount = p.childCount
                for (i in 0 until childCount) {
                    val child = p.getChildAt(i)
                    proxyOnClick(child, recycledContainerDeep)
                }
            } else {
                getClickListenerForView(view)
            }
        }
    }

    private fun getClickListenerForView(view: View) {
        try {
            val viewClazz = Class.forName("android.view.View")
            //事件监听器都是这个实例保存的
            val listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo")
            if (!listenerInfoMethod.isAccessible) {
                listenerInfoMethod.isAccessible = true
            }
            val listenerInfoObj = listenerInfoMethod.invoke(view)
            val listenerInfoClazz = Class.forName("android.view.View\$ListenerInfo")
            val onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener")
            if (!onClickListenerField.isAccessible) {
                onClickListenerField.isAccessible = true
            }
            val mOnClickListener = onClickListenerField[listenerInfoObj] as View.OnClickListener
            if (mOnClickListener !is ProxyOnclickListener) {
                //自定义代理事件监听器
                val onClickListenerProxy: View.OnClickListener =
                    ProxyOnclickListener(
                        mOnClickListener
                    )
                //更换
                onClickListenerField[listenerInfoObj] = onClickListenerProxy
            } else {
                e(
                    "OnClickListenerProxy",
                    "setted proxy listener "
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class ProxyOnclickListener internal constructor(private val onclick: View.OnClickListener?) :
        View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            //点击时间控制
            val currentTime = System.currentTimeMillis()
            val MIN_CLICK_DELAY_TIME = 500
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime
                onclick?.onClick(v)
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="app是否在后台运行">
    fun isAppOnForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfos = activityManager.getRunningTasks(100)
        val packageName = packageName
        //100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
        for (info in runningTaskInfos) {
            if (info.topActivity.packageName == packageName || info.baseActivity.packageName == packageName) {
                return true
            }
        }
        return false
    }
    // </editor-fold>

}