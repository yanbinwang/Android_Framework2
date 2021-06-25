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
        var available = false
        @Volatile
        var netState = -1//WIFI网络=0 蜂窝网络=1 其他网络（未知网络，包括蓝牙、VPN、LoWPAN）=-1
    }

    /**
     * 网络可用的回调
     */
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        available = true
    }

    /**
     * 网络丢失的回调
     */
    override fun onLost(network: Network) {
        super.onLost(network)
        available = false
    }

    /**
     * 在网络失去连接的时候回调，但是如果是生硬的断开连接，可能不回调
     */
    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
    }

    /**
     * 如果在超时时间内都没有找到可用的网络时进行回调
     */
    override fun onUnavailable() {
        super.onUnavailable()
    }

    /**
     * 当建立网络连接时，回调连接的属性
     */
    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
    }

    /**
     * 当网络发生了变化回调
     */
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            netState = when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 0
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 1
                else -> -1
            }
        }
    }

}