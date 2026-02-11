package com.example.common.network.socket.topic

import androidx.lifecycle.LifecycleOwner
import com.example.common.config.ServerConfig
import com.example.common.network.socket.topic.interf.SocketObserver
import com.example.framework.utils.function.value.safeGet

/**
 * socket连接帮助类
 * 1) 整体连接的订阅和关闭
 * 2) 登出时记得调取WebSocketConnect.disconnect()
 */
object WebSocketConnect {
    private val proxy by lazy { WebSocketTopic(ServerConfig.socketUrl()) }

    @JvmStatic
    fun topic(owner: LifecycleOwner, vararg destinations: String) {
        proxy.topic(owner, *destinations)
    }

    @JvmStatic
    fun untopic(vararg destinations: String) {
        proxy.untopic(*destinations)
    }

    @JvmStatic
    fun sendTo(owner: LifecycleOwner, data: String, position: Int = 0) {
        val socketAnnotation = owner::class.java.getAnnotation(SocketObserver::class.java) ?: return
        val destination = socketAnnotation.value.toList().safeGet(position) ?: ServerConfig.socketUrl()
        proxy.sendTo(destination, data)
    }

    @JvmStatic
    fun sendTo(destination: String = ServerConfig.socketUrl(), data: String) {
        proxy.sendTo(destination, data)
    }

    @JvmStatic
    fun disconnect() {
        proxy.disconnect()
    }

}