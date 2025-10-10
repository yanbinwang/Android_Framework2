package com.example.common.network.socket.topic

import androidx.lifecycle.LifecycleOwner
import com.example.common.config.ServerConfig

/**
 * socket连接帮助类
 * 整体连接的订阅和关闭
 * 登出时记得调取WebSocketConnect.disconnect()
 */
object WebSocketConnect {
    private val proxy by lazy { WebSocketTopic(ServerConfig.socketUrl()) }

    @JvmStatic
    fun topic(owner: LifecycleOwner, vararg destinations:  String) {
        proxy.topic(owner, *destinations)
    }

    @JvmStatic
    fun untopic(vararg destinations: String) {
        proxy.untopic(*destinations)
    }

    @JvmStatic
    fun disconnect() {
        proxy.disconnect()
    }

}