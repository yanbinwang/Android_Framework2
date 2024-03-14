package com.example.common.socket

import com.example.common.config.ServerConfig

/**
 * socket连接helper
 */
object WebSocketConnect {
    private val proxy by lazy { WebSocketProxy(ServerConfig.socketUrl()) }

//    fun connect(topicUrl: String) {
//        proxy.connect(topicUrl)
//    }

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