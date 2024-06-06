package com.example.mvvm.utils.socket.topic

import com.example.common.config.ServerConfig

/**
 * socket连接helper
 * 整体连接的订阅和关闭
 * 登出时记得调取WebSocketConnect.disconnect()
 */
object WebSocketConnect {
    private val proxy by lazy { WebSocketTopic(ServerConfig.socketUrl()) }

    @JvmStatic
    fun topic(vararg destinations:  String) {
        proxy.topic(*destinations)
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