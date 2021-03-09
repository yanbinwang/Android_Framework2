package com.example.common.utils.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.model.GuidePage
import com.example.common.constant.Constants
import com.tencent.mmkv.MMKV
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 *  Created by wangyanbin
 *  应用配置工具类
 */
@SuppressLint("MissingPermission", "HardwareIds", "StaticFieldLeak")
object ConfigHelper {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private var context: Context? = null

    fun initialize(application: Application) {
        context = application
        //在程序运行时取值，保证长宽静态变量不丢失
        val metric = DisplayMetrics()
        val mWindowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.defaultDisplay.getMetrics(metric)
        //屏幕宽度（像素）
        Constants.SCREEN_WIDTH = metric.widthPixels
        //屏幕高度（像素）
        Constants.SCREEN_HEIGHT = metric.heightPixels
        //获取手机的导航栏高度
        Constants.STATUS_BAR_HEIGHT = context!!.resources.getDimensionPixelSize(context!!.resources.getIdentifier("status_bar_height", "dimen", "android"))
        //获取手机的网络ip
        Constants.IP = getIp()
        //获取手机的Mac地址
        Constants.MAC = getMac()
        //获取手机的DeviceId
        Constants.DEVICE_ID = getDeviceId()
        //版本名，版本号
        Constants.VERSION_CODE = getAppVersionCode()
        Constants.VERSION_NAME = getAppVersionName()
        //获取应用名。包名。默认保存文件路径
        Constants.APPLICATION_FILE_PATH = Constants.SDCARD_PATH + "/" + Constants.APPLICATION_NAME
    }

    //遮罩引导
    fun showGuide(activity: Activity, label: String, vararg pages: GuidePage) {
        if (!obtainBehavior(label)) {
            storageBehavior(label, true)
            val weakActivity = WeakReference(activity)
            val builder = NewbieGuide.with(weakActivity.get())//传入activity
                .setLabel(label)//设置引导层标示，用于区分不同引导层，必传！否则报错
                .alwaysShow(true)
            for (page in pages) {
                builder.addGuidePage(page)
            }
            builder.show()
        }
    }

    //获取当前标签的行为-是否第一次启动，是否进入引导页等，针对用户的行为在用户类中单独管理
    fun obtainBehavior(label: String): Boolean {
        return mmkv.decodeBool(label, false)
    }

    //存储当前想标签行为
    fun storageBehavior(label: String, value: Boolean): Boolean {
        return mmkv.encode(label, value)
    }

    //模拟触屏点击屏幕事件
    fun touch(view: View) {
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        val upEvent = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0)
        view.onTouchEvent(downEvent)
        view.onTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
    }

    //获取当前设备ip地址
    private fun getIp(): String? {
        val networkInfo = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            //当前使用2G/3G/4G网络
            if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                try {
                    val enumeration = NetworkInterface.getNetworkInterfaces()
                    while (enumeration.hasMoreElements()) {
                        val networkInterface = enumeration.nextElement()
                        val inetAddresses = networkInterface.inetAddresses
                        while (inetAddresses.hasMoreElements()) {
                            val inetAddress = inetAddresses.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress.getHostAddress()
                            }
                        }
                    }
                } catch (e: SocketException) {
                    return null
                }
                //当前使用无线网络
            } else if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                val wifiManager = context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                //得到IPV4地址
                return initIp(wifiInfo.ipAddress)
            }
        }
        return null
    }

    //将ip的整数形式转换成ip形式
    private fun initIp(ipInt: Int): String {
        return (ipInt and 0xFF).toString() + "." +
                (ipInt shr 8 and 0xFF) + "." +
                (ipInt shr 16 and 0xFF) + "." +
                (ipInt shr 24 and 0xFF)
    }

    //获取当前设备的mac地址
    private fun getMac(): String? {
        try {
            val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return null
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //获取当前设备的id
    private fun getDeviceId(): String? {
        return try {
            (context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
        } catch (e: SecurityException) {
            null
        }
    }

    //获取当前app version code
    private fun getAppVersionCode(): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = context!!.applicationContext.packageManager.getPackageInfo(context?.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        return appVersionCode
    }

    //获取当前app version name
    private fun getAppVersionName(): String {
        var appVersionName = ""
        try {
            val packageInfo = context!!.applicationContext.packageManager.getPackageInfo(context?.packageName, 0)
            appVersionName = packageInfo.versionName
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        return appVersionName
    }

}