package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager

import com.example.common.BaseApplication

/**
 * author: wyb
 * date: 2018/8/13.
 * 网路监测类
 */
@SuppressLint("StaticFieldLeak", "MissingPermission")
object NetWorkUtil {
    private val context by lazy { BaseApplication.instance?.applicationContext!! }
    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private const val SECURITY_NONE = 0
    private const val SECURITY_WEP = 1
    private const val SECURITY_PSK = 2
    private const val SECURITY_EAP = 3

    /**
     * 验证是否联网
     */
    @JvmStatic
    fun isNetworkAvailable(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return networkInfo.state == NetworkInfo.State.CONNECTED
        }
        return false
    }

    /**
     * 判断当前网络环境是否为wifi
     */
    @JvmStatic
    fun isWifi(): Boolean {
        try {
            return connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        } catch (ignored: Exception) {
        }
        return false
    }

    /**
     * 无线网络=1 移动网络=0 没有连接网络=-1
     */
    @JvmStatic
    fun getNetWorkState(): Int {
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return 1
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return 0
            }
        } else return -1
        return -1
    }

    /**
     * 获取当前wifi密码的加密策略
     */
    @JvmStatic
    fun getWifiSecurity(): String {
        var result = "NONE"
        if (isWifi()) {
            val mWifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = mWifiManager.connectionInfo
            //得到配置好的网络连接
            val wifiConfigList = mWifiManager.configuredNetworks
            for (wifiConfiguration in wifiConfigList) {
                //配置过的SSID
                var configSSid = wifiConfiguration.SSID
                configSSid = configSSid.replace("\"", "")
                //当前连接SSID
                var currentSSid = info.ssid
                currentSSid = currentSSid.replace("\"", "")
                //比较networkId，防止配置网络保存相同的SSID
                if (currentSSid.equals(configSSid) && (info.networkId == wifiConfiguration.networkId)) {
                    result = when (getSecurity(wifiConfiguration)) {
                        0 -> "NONE"
                        1 -> "WPA_EAP"
                        2 -> "WPA_PSK"
                        else -> "IEEE8021X"
                    }
                }
            }
        }
        return result
    }

    private fun getSecurity(config: WifiConfiguration): Int {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) return SECURITY_PSK
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(
                WifiConfiguration.KeyMgmt.IEEE8021X)) return SECURITY_EAP
        return if (config.wepKeys[0] != null) SECURITY_WEP else SECURITY_NONE
    }

}