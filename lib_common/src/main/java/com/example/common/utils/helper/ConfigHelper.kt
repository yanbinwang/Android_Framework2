package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.core.Controller
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.base.utils.function.color
import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.utils.builder.StatusBarBuilder
import com.tencent.mmkv.MMKV
import java.lang.ref.WeakReference

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
@SuppressLint("MissingPermission", "HardwareIds", "StaticFieldLeak")
object ConfigHelper {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private lateinit var context: Context

    @JvmStatic
    fun initialize(application: Application) {
        context = application
        //在程序运行时取值，保证长宽静态变量不丢失
        val metrics = context.resources.displayMetrics
        //屏幕宽度（像素）
        Constants.SCREEN_WIDTH = metrics.widthPixels
        //屏幕高度（像素）
        Constants.SCREEN_HEIGHT = metrics.heightPixels
        //屏幕比值 (dp)
        Constants.SCREEN_DENSITY = metrics.densityDpi
        //获取手机的导航栏高度
        Constants.STATUS_BAR_HEIGHT = context.resources.getDimensionPixelSize(context.resources.getIdentifier("status_bar_height", "dimen", "android"))
//        //获取手机的网络ip
//        Constants.IP = getIp()
//        //获取手机的Mac地址
//        Constants.MAC = getMac()
//        //获取手机的DeviceId
//        Constants.DEVICE_ID = getDeviceId()
        //版本名，版本号
        Constants.VERSION_CODE = getAppVersionCode()
        Constants.VERSION_NAME = getAppVersionName()
        //获取应用名。包名。默认保存文件路径
        Constants.APPLICATION_FILE_PATH = "${Constants.SDCARD_PATH}/${Constants.APPLICATION_NAME}"
    }

    /**
     * 遮罩引导
     * https://www.jianshu.com/p/f28603e59318
     * ConfigHelper.showGuide(this,"sdd",GuidePage
     *  .newInstance()
     *  .addHighLight(binding.btnList)
     *  .setBackgroundColor(color(R.color.black_4c000000))
     *  .setLayoutRes(R.layout.view_guide_simple))
     */
    @JvmStatic
    fun showGuide(activity: Activity, label: String, vararg pages: GuidePage, color: Int = R.color.white) {
        if (!decodeBool(label)) {
            encode(label, true)
            val weakActivity = WeakReference(activity)
            val statusBarBuilder = StatusBarBuilder(weakActivity.get()!!.window)
            val builder = NewbieGuide.with(weakActivity.get())//传入activity
                .setLabel(label)//设置引导层标示，用于区分不同引导层，必传！否则报错
                .setOnGuideChangedListener(object : OnGuideChangedListener {
                    override fun onShowed(controller: Controller?) {
                        statusBarBuilder.setStatusBarColor(activity.color(R.color.black_4c000000))
                    }

                    override fun onRemoved(controller: Controller?) {
                        statusBarBuilder.setStatusBarColor(activity.color(color))
                    }
                })
                .alwaysShow(true)
            for (page in pages) {
                //此处处理一下阴影背景
                page.backgroundColor = activity.color(R.color.black_4c000000)
                builder.addGuidePage(page)
            }
            builder.show()
        }
    }

    /**
     * 存储当前标签行为
     */
    @JvmStatic
    fun encode(key: String, value: Boolean) = mmkv.encode(key, value)

    @JvmStatic
    fun encode(key: String, value: String) = mmkv.encode(key, value)

    @JvmStatic
    fun <T : Parcelable> encode(key: String, value: T) = mmkv.encode(key, value)

    /**
     * 获取当前标签的行为-是否第一次启动，是否进入引导页等，针对用户的行为在用户类中单独管理
     */
    @JvmStatic
    fun decodeBool(key: String) = mmkv.decodeBool(key, false)

    @JvmStatic
    fun decodeString(key: String) = mmkv.decodeString(key, "")

    @JvmStatic
    fun <T : Parcelable> decodeParcelable(label: String, tClass: Class<T>) = mmkv.decodeParcelable(label, tClass)

    @JvmStatic
    fun removeValueForKey(label: String) = mmkv.removeValueForKey(label)

    /**
     * 在进程中去寻找当前APP的信息，判断是否在运行
     * 100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
     */
    fun isAppOnForeground(): Boolean {
        val processes = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses ?: return false
        for (process in processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(context.packageName)) return true
        }
        return false
    }

//    /**
//     * 获取当前设备ip地址
//     */
//    private fun getIp(): String {
//        val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (networkCapabilities != null) {
//                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    return getMobileIp()
//                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                    return getWifiIp()
//                }
//            }
//        } else {
//            val networkInfo = connectivityManager.activeNetworkInfo
//            if (networkInfo != null && networkInfo.isConnected) {
//                if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
//                    return getMobileIp()
//                } else if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
//                    return getWifiIp()
//                }
//            }
//        }
//        return ""
//    }
//
//    private fun getMobileIp(): String {
//        try {
//            val enumeration = NetworkInterface.getNetworkInterfaces()
//            while (enumeration.hasMoreElements()) {
//                val networkInterface = enumeration.nextElement()
//                val inetAddresses = networkInterface.inetAddresses
//                while (inetAddresses.hasMoreElements()) {
//                    val inetAddress = inetAddresses.nextElement()
//                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) return inetAddress.getHostAddress()
//                }
//            }
//        } catch (ignored: SocketException) {
//            return ""
//        }
//        return ""
//    }
//
//    private fun getWifiIp(): String {
//        val wifiManager = context.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo = wifiManager.connectionInfo
//        val ipInt = wifiInfo.ipAddress
//        return (ipInt and 0xFF).toString() + "." +
//                (ipInt shr 8 and 0xFF) + "." +
//                (ipInt shr 16 and 0xFF) + "." +
//                (ipInt shr 24 and 0xFF)
//    }
//
//    /**
//     * 获取当前设备的mac地址
//     */
//    private fun getMac(): String? {
//        try {
//            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
//            for (nif in all) {
//                if (!nif.name.equals("wlan0")) continue
//                val macBytes = nif.hardwareAddress ?: return null
//                val res1 = StringBuilder()
//                for (b in macBytes) {
//                    res1.append(String.format("%02X:", b))
//                }
//                if (res1.isNotEmpty()) res1.deleteCharAt(res1.length - 1)
//                return res1.toString()
//            }
//        } catch (ignored: Exception) {
//        }
//        return null
//    }
//
//    /**
//     * 获取当前设备的id
//     */
//    private fun getDeviceId(): String? {
//        val telephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
//        return try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                telephonyManager.imei
//            } else {
//                telephonyManager.deviceId
//            }
//        } catch (ignored: SecurityException) {
//            null
//        }
//    }

    /**
     * 获取当前app version code
     */
    private fun getAppVersionCode(): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = context.applicationContext.packageManager.getPackageInfo(context.packageName!!, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    private fun getAppVersionName(): String {
        var appVersionName = ""
        try {
            val packageInfo = context.applicationContext.packageManager.getPackageInfo(context.packageName!!, 0)
            appVersionName = packageInfo.versionName
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        return appVersionName
    }

}