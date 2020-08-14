package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager

import com.example.common.BaseApplication

/**
 * author: wyb
 * date: 2018/8/13.
 * 网路监测类
 */
@SuppressLint("StaticFieldLeak", "MissingPermission")
object NetWorkUtil {
    //等效于懒加载，使用时取值，之后复用
    private val context by lazy {
        BaseApplication.instance?.applicationContext!!
    }

    //验证是否联网
    @JvmStatic
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            //当前网络是连接的
            return networkInfo.state == NetworkInfo.State.CONNECTED
        }
        return false
    }

    //无线网络=1 移动网络=0 没有连接网络=-1
    @JvmStatic
    fun getNetWorkState(): Int {
        //得到连接管理器对象
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return 1
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return 0
            }
        } else {
            return -1
        }
        return -1
    }

    //判断当前网络环境是否为wifi
    @JvmStatic
    fun isWifi(): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivity.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    //获取网络状态
    @JvmStatic
    fun getAPNType(): String {
        var netType = ""
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo ?: return "NULL"
        val nType = networkInfo.type
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "wifi"
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            val nSubType = networkInfo.subtype
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            netType = when {
                nSubType == TelephonyManager.NETWORK_TYPE_LTE && !telephonyManager.isNetworkRoaming -> "4G"
                nSubType == TelephonyManager.NETWORK_TYPE_UMTS || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0 && !telephonyManager.isNetworkRoaming -> "3G"
                nSubType == TelephonyManager.NETWORK_TYPE_GPRS || nSubType == TelephonyManager.NETWORK_TYPE_EDGE || nSubType == TelephonyManager.NETWORK_TYPE_CDMA && !telephonyManager.isNetworkRoaming -> "2G"
                else -> "mobile"
            }
        }
        return netType
    }

}