package com.example.socket.utils

import com.example.socket.config.SocketConfig

/**
 * socket连接helper
 */
object WebSocketConnect {
    private val proxy by lazy { WebSocketProxy(SocketConfig.socketUrl()) }

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