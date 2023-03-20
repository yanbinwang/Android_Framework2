package com.example.socket.helper

import com.example.socket.WebSocketProxy

/**
 * @description socket連接幫助類
 * @author yan
 */
object SocketConnectHelper {
    private val proxy = WebSocketProxy("11111")

    fun topic(vararg value: String) {
        proxy.topic(*value)
    }

    fun untopic(vararg value: String) {
        proxy.untopic(*value)
    }

    fun disconnect() {
        proxy.disconnect()
    }

}