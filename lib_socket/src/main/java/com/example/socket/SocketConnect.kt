package com.example.socket

/**
 * @description socket連接幫助類
 * @author yan
 */
object SocketConnect {
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