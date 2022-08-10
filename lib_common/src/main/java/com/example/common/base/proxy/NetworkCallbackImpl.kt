package com.example.common.base.proxy

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

/**
 *  Created by wangyanbin
 *  网络监听
 */
class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
    companion object {
        @Volatile
        var netState = -1//WIFI网络=0 蜂窝网络=1 其他网络（未知网络，包括蓝牙、VPN、LoWPAN）=-1
    }

    /**
     * 网络连接成功回调
     */
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
    }

    /**
     * 网络连接超时或网络不可达
     */
    override fun onUnavailable() {
        super.onUnavailable()
    }

    /**
     * 网络已断开连接
     */
    override fun onLost(network: Network) {
        super.onLost(network)
    }

    /**
     * 网络正在丢失连接
     */
    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
    }

    /**
     * 网络状态变化
     * NetworkCapabilities.NET_CAPABILITY_INTERNET->表示是否连接上了互联网（不关心是否可以上网）
     * NetworkCapabilities.NET_CAPABILITY_VALIDATED->表示能够和互联网通信（这个为true表示能够上网）
     */
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        netState = if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 0
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 1
                else -> -1
            }
        } else -1
    }

    /**
     * 网络连接属性变化
     */
    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
    }

    /**
     * 访问的网络阻塞状态发生变化
     */
    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
    }

}