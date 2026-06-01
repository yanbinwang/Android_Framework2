package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Handler
import android.os.Looper
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
@SuppressLint("MissingPermission")
class WifiUtil(private val mActivity: FragmentActivity) {
    private var currentCallback: ((List<ScanResult>) -> Unit)? = null
    private val wifiManager by lazy { mActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val connectivityManager by lazy { mActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private val resultsCallback by lazy {
        @RequiresApi(Build.VERSION_CODES.R)
        object : WifiManager.ScanResultsCallback() {
            override fun onScanResultsAvailable() {
                try {
                    val list = wifiManager.scanResults.filter { !it.SSID.isNullOrBlank() }
                    currentCallback?.invoke(list)
                } catch (_: SecurityException) {
                    currentCallback?.invoke(emptyList())
                } finally {
                    unregisterScan()
                }
            }
        }
    }
    private val receiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val list = wifiManager.scanResults.filter { !it.SSID.isNullOrBlank() }
                    currentCallback?.invoke(list)
                    unregisterScan()
                }
            }
        }
    }

    init {
        mActivity.doOnDestroy {
            disconnectWifi()
            unregisterScan()
        }
    }

    /**
     * 连接 Wi-Fi
     */
    fun connectWifi(ssid: String, password: String, isOpenWifi: Boolean = false, onConnected: () -> Unit, onFailed: () -> Unit) {
        // 先打开 Wi-Fi（只有关闭时才打开）
        if (!isWifiEnabled()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 代码无法打开Wi-Fi → 跳系统设置
                    val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    mActivity.startActivity(intent)
                } else {
                    // Android 9 及以下 → 代码安全打开
                    wifiManager.isWifiEnabled = true
                }
            } catch (_: Exception) {
                onFailed()
                return
            }
        }
        // 建立连接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 官方标准方案（稳定、target37 专用）
            connectAndroid10Plus(ssid, password, isOpenWifi, onConnected, onFailed)
        } else {
            // Android 9 及以下老方案
            connectLegacy(ssid, password, isOpenWifi, onConnected, onFailed)
        }
    }

    /**
     * Android 10+ 连接
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectAndroid10Plus(ssid: String, password: String, isOpenWifi: Boolean, onConnected: () -> Unit, onFailed: () -> Unit) {
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
        }, Handler(Looper.getMainLooper()))
    }

    /**
     * Android 9 及以下连接
     */
    @SuppressLint("MissingPermission")
    private fun connectLegacy(ssid: String, password: String, isOpenWifi: Boolean, onConnected: () -> Unit, onFailed: () -> Unit) {
        try {
            // 先移除同SSID旧配置
            wifiManager.configuredNetworks?.forEach { cfg ->
                if (cfg.SSID == "\"$ssid\"") {
                    wifiManager.removeNetwork(cfg.networkId)
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
        } catch (_: Exception) {
            onFailed()
        }
    }

    /**
     * 断开当前 Wi-Fi
     */
    fun disconnectWifi() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectivityManager.bindProcessToNetwork(null)
            } else {
                wifiManager.disconnect()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 注册扫描
     */
    fun registerScan(onResult: (List<ScanResult>) -> Unit) {
        if (!isWifiEnabled()) {
            onResult(emptyList())
            return
        }
        currentCallback = onResult
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+：注册回调 → 触发扫描
            wifiManager.registerScanResultsCallback(mActivity.mainExecutor, resultsCallback)
            wifiManager.startScan()
        } else {
            // Android 10 及以下：广播方式
            mActivity.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiManager.startScan()
        }
    }

    /**
     * 注销扫描
     */
    private fun unregisterScan() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiManager.unregisterScanResultsCallback(resultsCallback)
            } else {
                mActivity.unregisterReceiver(receiver)
            }
        } catch (_: Exception) {
        } finally {
            currentCallback = null
        }
    }

    /**
     * 获取当前连接的 Wi-Fi 名称
     */
    fun getCurrentSsid(): String? {
        if (!isWifiEnabled()) return null
        val activeNetwork = connectivityManager.activeNetwork ?: return null
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null
        // 不是 Wi-Fi 网络直接返回 null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return null
        }
        val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            caps.transportInfo as? WifiInfo
        } else {
            wifiManager.connectionInfo
        } ?: return null
        val ssid = wifiInfo.ssid
        return if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") {
            null
        } else {
            ssid.removeSurrounding("\"")
        }
    }

    /**
     * 判断 Wi-Fi 是否打开
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

}