package com.example.qiniu.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.qiniu.android.dns.DnsManager
import com.qiniu.android.dns.IResolver
import com.qiniu.android.dns.NetworkInfo
import com.qiniu.android.dns.Record
import com.qiniu.android.dns.dns.DnsUdpResolver
import com.qiniu.android.dns.dns.DohResolver
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

object Util {

    @JvmStatic
    fun isSupportScreenCapture(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    @JvmStatic
    fun isSupportHWEncode(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    }

    @JvmStatic
    fun showToast(activity: Activity, msg: String?) {
        showToast(activity, msg, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun showToast(activity: Activity, msg: String?, duration: Int) {
        activity.runOnUiThread { Toast.makeText(activity, msg, duration).show() }
    }

    @JvmStatic
    fun syncRequest(appServerUrl: String?): String? {
        try {
            val httpConn = URL(appServerUrl).openConnection() as? HttpURLConnection
            httpConn?.setRequestMethod("GET")
            httpConn?.setConnectTimeout(5000)
            httpConn?.setReadTimeout(10000)
            val responseCode = httpConn?.getResponseCode()
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }
            var length = httpConn.getContentLength()
            if (length <= 0) {
                length = 16 * 1024
            }
            val inputStream = httpConn.inputStream
            val data = ByteArray(length)
            val read = inputStream.read(data)
            inputStream.close()
            return if (read <= 0) {
                null
            } else String(data, 0, read)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 配置自定义 DNS 服务器，非必须
     *
     * - 可通过创建 [DnsUdpResolver] 对象配置自定义的 DNS 服务器地址
     * - 可通过创建 [DohResolver] 对象配置支持 Doh(Dns over http) 协议的 url
     * 其中，UDP 的方式解析速度快，但是安全性无法得到保证，HTTPDNS 的方式解析速度慢，但是安全性有保证，您可根据您的
     * 使用场景自行选择合适的解析方式
     */
    fun getMyDnsManager(): DnsManager {
        val resolvers = arrayOfNulls<IResolver>(2)
        // 配置自定义 DNS 服务器地址
        val udpDnsServers = arrayOf("223.5.5.5")
        resolvers[0] = DnsUdpResolver(udpDnsServers, Record.TYPE_A, IResolver.DNS_DEFAULT_TIMEOUT)
        // 配置 HTTPDNS 地址
        val httpDnsServers = arrayOf("https://223.6.6.6/dns-query")
        resolvers[1] = DohResolver(httpDnsServers, Record.TYPE_A, IResolver.DNS_DEFAULT_TIMEOUT)
        return DnsManager(NetworkInfo.normal, resolvers)
    }

    fun getVersion(context: Context): String {
        val packageManager = context.packageManager
        try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getUserId(context: Context): String? {
        val preferences = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
        var userId = preferences.getString(Config.KEY_USER_ID, "")
        if ("" == userId) {
            userId = userId()
            val editor = preferences.edit()
            editor.putString(Config.KEY_USER_ID, userId)
            editor.apply()
        }
        return userId
    }

    private fun userId(): String {
        val r = Random()
        return System.currentTimeMillis().toString() + "" + r.nextInt(999)
    }

}