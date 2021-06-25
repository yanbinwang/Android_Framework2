package com.example.common.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.example.base.utils.LogUtil
import com.example.common.BaseApplication
import com.example.common.base.proxy.NetworkCallbackImpl

/**
 * author: wyb
 * date: 2018/8/13.
 * 网路监测类
 */
object NetWorkUtil {
    private val context by lazy { BaseApplication.instance?.applicationContext!! }

    /**
     * 验证是否联网
     */
    @JvmStatic
    fun isNetworkAvailable() = NetworkCallbackImpl.available

    /**
     * 判断当前网络环境是否为wifi
     */
    @JvmStatic
    fun isWifi() = getNetWorkState() == 0

    /**
     * WIFI网络=0 蜂窝网络=1 其他网络（未知网络，包括蓝牙、VPN、LoWPAN）=-1
     */
    @JvmStatic
    fun getNetWorkState() = NetworkCallbackImpl.netState

    /**
     * 获取当前wifi密码的加密策略(需要定位权限)
     * 无线路由器里带有的加密模式主要有：WEP，WPA-PSK（TKIP），WPA2-PSK（AES）和WPA-PSK（TKIP）+WPA2-PSK（AES）。
     * WPA2-PSK的加密方式基本无法破解，无线网络加密一般需要用此种加密方式才可以有效防止不被蹭网，考虑到设备兼容性，有WPA-PSK（TKIP）+WPA2-PSK（AES）混合加密选项的话一般选择此项，加密性能好，兼容性也广。
     * WEP是Wired Equivalent Privacy（有线等效保密）的英文缩写，目前常见的是64位WEP加密和128位WEP加密。它是一种最老也是最不安全的加密方式，不建议大家选用。
     * WPA是WEP加密的改进版，包含两种方式：预共享密钥和Radius密钥（远程用户拨号认证系统）。其中预共享密钥（pre-share key缩写为PSK）有两种密码方式：TKIP和AES，而RADIUS密钥利用RADIUS服务器认证并可以动态选择TKIP、AES、WEP方式。相比TKIP，AES具有更好的安全系数，建议用户使用。
     * WPA2即WPA加密的升级版。WPA2同样也分为TKIP和AES两种方式，因此也建议选AES加密不要选TKIP。
     */
    @JvmStatic
    fun getWifiSecurity(): String {
        var result = "NONE"
        if (isWifi()) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            for (scanResult in wifiManager.scanResults) {
                val capabilities = scanResult.capabilities
                LogUtil.e(" \nconnectionInfo的ssid:${connectionInfo.bssid}\nscanResult的ssid:${scanResult.BSSID}\ncapabilities:$capabilities")
                if (scanResult.BSSID.contains(connectionInfo.bssid)) {
                    result = when {
                        capabilities.contains("WPA2-PSK") -> "WPA2-PSK"
                        capabilities.contains("WPA2") -> "WPA2"
                        capabilities.contains("WEP") -> "WEP"
                        capabilities.contains("WPA") -> "WPA"
                        else -> "NONE"
                    }
                    break
                }
            }
        }
        return result
    }

}