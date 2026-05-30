package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.example.framework.utils.function.doOnDestroy

/**
 * Wifi 连接工具类
 * 1) 权限配置
 *  <!-- 基础Wi-Fi -->
 *  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 *  <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 *  <!-- Android 13+ 新权限 取代 定位权限 -->
 *  <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES"
 *     android:usesPermissionFlags="neverForLocation" />
 *  <!-- 旧版本兼容扫描 -->
 *  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"
 *     android:maxSdkVersion="32" />
 *  <uses-feature
 *     android:name="android.hardware.wifi"
 *     android:required="true" />
 */
class WifiUtil(private val mActivity: FragmentActivity) {
    private val wifiManager by lazy { mActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val connectivityManager by lazy { mActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    init {
        mActivity.doOnDestroy {
            disconnectWifi()
        }
    }

    /**
     * 连接 Wi-Fi
     */
    fun connectWifi(ssid: String, password: String, isOpenWifi: Boolean = false, onConnected: () -> Unit, onFailed: () -> Unit) {
        // 先打开 Wi-Fi
        enableWifiSafely(onFailed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 官方标准方案（稳定、target37 专用）
            connectAndroidQAndAbove(ssid, password, isOpenWifi, onConnected, onFailed)
        } else {
            // Android 9 及以下老方案
            connectBelowAndroidQ(ssid, password, isOpenWifi, onConnected, onFailed)
        }
    }

    /**
     * 打开 Wi-Fi
     */
    private fun enableWifiSafely(onFailed: () -> Unit) {
        if (wifiManager.isWifiEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 代码无法打开Wi-Fi → 跳系统设置
            try {
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                mActivity.startActivity(intent)
            } catch (_: Exception) {
                onFailed()
            }
        } else {
            // Android 9 及以下 → 代码安全打开
            wifiManager.isWifiEnabled = true
        }
    }

    /**
     * Android 10+ 连接
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectAndroidQAndAbove(ssid: String, password: String, isOpenWifi: Boolean, onConnected: () -> Unit, onFailed: () -> Unit) {
        // 构建 Wifi 连接
        val builder = WifiNetworkSpecifier.Builder().setSsid(ssid)
        // 开放网络不能设置密码
        if (!isOpenWifi) {
            builder.setWpa2Passphrase(password)
        }
        // 创建连接请求
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(builder.build())
            .build()
        // 发起连接请求
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // 让App走这个Wi-Fi
                connectivityManager.bindProcessToNetwork(network)
                onConnected()
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                onFailed()
                connectivityManager.unregisterNetworkCallback(this)
            }
        })
    }

    /**
     * Android 9 及以下连接
     */
    @SuppressLint("MissingPermission")
    private fun connectBelowAndroidQ(ssid: String, password: String, isOpenWifi: Boolean, onConnected: () -> Unit, onFailed: () -> Unit) {
        // 先移除同SSID旧配置（关键！否则经常失败）
        val existingNetworks = wifiManager.configuredNetworks
        if (existingNetworks != null) {
            for (cfg in existingNetworks) {
                if (cfg.SSID == "\"$ssid\"") {
                    wifiManager.removeNetwork(cfg.networkId)
                }
            }
        }
        // 构建配置
        val config = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            // 支持隐藏SSID
            hiddenSSID = false
            // 先全清，再只开需要的
            allowedAuthAlgorithms.clear()
            allowedGroupCiphers.clear()
            allowedKeyManagement.clear()
            allowedPairwiseCiphers.clear()
            allowedProtocols.clear()
            if (isOpenWifi) {
                // 开放网络
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            } else {
                // WPA/WPA2 PSK
                preSharedKey = "\"$password\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                // 显式打开常用 cipher，兼容性更好
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            }
        }
        // 添加并启用网络
        val netId = wifiManager.addNetwork(config)
        if (netId == -1) {
            onFailed()
            return
        }
        // 开始连接
        val enabled = wifiManager.enableNetwork(netId, true)
        // 必须 save 很多机型不 save 不生效
        wifiManager.saveConfiguration()
        if (enabled) {
            onConnected()
        } else {
            onFailed()
        }
    }

    /**
     * 断开当前 Wi-Fi
     */
    fun disconnectWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.bindProcessToNetwork(null)
        } else {
            wifiManager.disconnect()
        }
    }

    /**
     * 获取当前连接的 Wi-Fi 名称
     */
    fun getCurrentSsid(): String? {
        if (!wifiManager.isWifiEnabled) return null
        val info = wifiManager.connectionInfo
        return if (info.ssid == null || info.ssid == "<unknown ssid>") {
            null
        } else {
            info.ssid.replace("\"", "")
        }
    }

    /**
     * 判断 Wi-Fi 是否打开
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

}