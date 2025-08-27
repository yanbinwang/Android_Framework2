package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import com.example.common.BaseApplication
import com.example.common.event.EventCode.EVENT_OFFLINE
import com.example.common.event.EventCode.EVENT_ONLINE
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.logE

/**
 * author: wyb
 * date: 2018/8/13.
 * 网路监测类整体网络是异步监听的，如果想实时获取，需要主动调用
 * isAvailable->是否可以进行网络连接。当持续或半持续状态阻止连接到该网络时，网络不可用
 * isConnected->是否存在网络连接以及是否可以建立连接和传递数据
 */
@SuppressLint("MissingPermission", "ObsoleteSdkInt")
object NetWorkUtil {
    private val context by lazy { BaseApplication.instance.applicationContext }
    private val manager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager }

    /**
     * 网络变化监听(相比广播更精确)
     */
    @JvmStatic
    fun init(owner: LifecycleOwner) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            var listener: (isOnline: Boolean) -> Unit = {}
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                listener(isOnline)
            }
        }.apply {
            listener = {
                if (it) {
                    EVENT_ONLINE.post()
                } else {
                    EVENT_OFFLINE.post()
                }
            }
        }
        manager?.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), networkCallback)
        owner.doOnDestroy {
            manager?.unregisterNetworkCallback(networkCallback)
        }
    }

    /**
     * 验证是否联网,保证连接正常建立
     * 在 Android 14 及更高版本中，NetworkCapabilities.NET_CAPABILITY_VALIDATED 表示网络已经通过运营商验证并且可以访问互联网。
     * 但实际使用中，这个条件比较严格，很多时候即使网络正常也可能不满足
     */
    @JvmStatic
    fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val networkInfo = manager?.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) return networkInfo.state == NetworkInfo.State.CONNECTED
        } else {
            val network = manager?.activeNetwork ?: return false
            val capabilities = manager?.getNetworkCapabilities(network) ?: return false
            // 1. 检查是否有网络传输通道（Wi-Fi/蜂窝网络等）
            val hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            // 2. 检查网络是否已通过系统验证（可联网）
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            // 3.返回联网结果
            return hasTransport && isValidated
        }
        return false
    }

    /**
     * 判断当前网络环境是否为手机流量，只需校验是否是流量
     */
    @JvmStatic
    fun isMobileConnected(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return manager?.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
        } else {
            val network = manager?.activeNetwork ?: return false
            val capabilities = manager?.getNetworkCapabilities(network) ?: return false
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
        }
        return false
    }

    /**
     * 判断当前网络环境是否为wifi，只需校验是否是wifi
     */
    @JvmStatic
    fun isWifiConnected(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return manager?.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        } else {
            val network = manager?.activeNetwork ?: return false
            val capabilities = manager?.getNetworkCapabilities(network) ?: return false
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
        }
        return false
    }

    /**
     * 是否挂载了网络代理（wifi模式下）
     */
    @JvmStatic
    fun isMountAgent(): Boolean {
        val httpProxy = !System.getProperty("http.proxyHost").isNullOrEmpty() && (System.getProperty("http.proxyPort")?.toIntOrNull() ?: -1) != -1
        val httpsProxy = !System.getProperty("https.proxyHost").isNullOrEmpty() && (System.getProperty("https.proxyPort")?.toIntOrNull() ?: -1) != -1
        return httpProxy || httpsProxy
    }

    /**
     * 是否挂载了VPN，只需校验是否是Vpn
     */
    @JvmStatic
    fun isMountVpn(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return manager?.activeNetworkInfo?.type == ConnectivityManager.TYPE_VPN
        } else {
            val network = manager?.activeNetwork ?: return false
            val capabilities = manager?.getNetworkCapabilities(network) ?: return false
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true
        }
        return false
    }

    /**
     * 获取当前wifi密码的加密策略(需要定位权限)
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     * 无线路由器里带有的加密模式主要有：WEP，WPA-PSK（TKIP），WPA2-PSK（AES）和WPA-PSK（TKIP）+WPA2-PSK（AES）。
     * WPA2-PSK的加密方式基本无法破解，无线网络加密一般需要用此种加密方式才可以有效防止不被蹭网，考虑到设备兼容性，有WPA-PSK（TKIP）+WPA2-PSK（AES）混合加密选项的话一般选择此项，加密性能好，兼容性也广。
     * WEP是Wired Equivalent Privacy（有线等效保密）的英文缩写，目前常见的是64位WEP加密和128位WEP加密。它是一种最老也是最不安全的加密方式，不建议大家选用。
     * WPA是WEP加密的改进版，包含两种方式：预共享密钥和Radius密钥（远程用户拨号认证系统）。其中预共享密钥（pre-share key缩写为PSK）有两种密码方式：TKIP和AES，而RADIUS密钥利用RADIUS服务器认证并可以动态选择TKIP、AES、WEP方式。相比TKIP，AES具有更好的安全系数，建议用户使用。
     * WPA2即WPA加密的升级版。WPA2同样也分为TKIP和AES两种方式，因此也建议选AES加密不要选TKIP。
     */
    @JvmStatic
    fun getWifiSecurity(): String {
        var result = "NONE"
        if (isWifiConnected()) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return result
            val connectionInfo = wifiManager.connectionInfo
            for (scanResult in wifiManager.scanResults) {
                val capabilities = scanResult.capabilities
                " \nconnectionInfo的ssid:${connectionInfo.bssid}\nscanResult的ssid:${scanResult.BSSID}\ncapabilities:$capabilities".logE
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