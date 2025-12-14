package com.example.common.base.proxy

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * @description 监听网络的改变状态,只有在用户操作网络连接开关(wifi,mobile)的时候接受广播
 * @author yan
 */
@SuppressLint("MissingPermission")
@Deprecated("可删,详细看NetWorkUtil的init方法")
class NetworkReceiver : BroadcastReceiver() {
    companion object {
        val filter by lazy {
            IntentFilter().apply {
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
                addAction("android.net.wifi.WIFI_STATE_CHANGED")
                addAction("android.net.wifi.STATE_CHANGE")
            }
        }
    }

    var listener: (online: Boolean) -> Unit = {}

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val activeNetwork = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo
            when {
                activeNetwork == null -> listener(false)
                activeNetwork.isConnected -> listener(true)
                else -> listener(false)
            }
        }
    }

}