package com.example.common.socket.helper

import com.example.common.socket.WebSocketProxy

/**
 * @description socket連接幫助類
 * @author yan
 */
object SocketConnectHelper {
    private val proxy = WebSocketProxy("11111")

    fun topic(topicUrl: String) {
        proxy.topic(topicUrl)
    }

    fun untopic(topicUrl: String) {
        proxy.untopic(topicUrl)
    }

    fun disconnect() {
        proxy.disconnect()
    }

}