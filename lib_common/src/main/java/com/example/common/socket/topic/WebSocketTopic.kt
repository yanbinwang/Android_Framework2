package com.example.common.socket.topic

import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.common.socket.WebSocketProxy
import com.example.common.utils.helper.AccountHelper.isLogin

/**
 * 如果页面是需要订阅多个地址的，实现当前页面
 */
class WebSocketTopic(private val socketUrl: String) {
    private val proxy by lazy { WebSocketProxy(socketUrl) }
    private val list by lazy { ArrayList<String>() }

    companion object {
        internal var listener: (url: String?, data: Message?) -> Unit = { _, _ -> }

        /**
         * 再baseapplication中实现回调，通过evenbus分发对应接受的消息
         */
        fun setOnMessageListener(listener: (url: String?, data: Message?) -> Unit) {
            this.listener = listener
        }
    }

    init {
        proxy.setOnWebSocketProxyListener(object : WebSocketProxy.OnWebSocketProxyListener {
            override fun onConnected(onConnected: Stomp) {
                topicNow()
            }

            override fun onDisconnected(onConnected: WebSocket.Close) {
            }

            override fun onError(onConnected: Message) {
            }

            override fun onException(onConnected: Throwable) {
            }
        })
    }

    /**
     *  订阅服务提供的topic
     */
    fun topic(vararg destinations: String) {
        //未登录不订阅
        if (!isLogin()) return
        if (!proxy.isConnected()) {
            list.clear()
            list.addAll(destinations.toList())
            proxy.connect()
            return
        }
        topicNow()
    }

    /**
     *  订阅服务提供的topic
     */
    private fun topicNow() {
        list.forEach { destination ->
            proxy.topic(destination) { _: String?, data: Message? ->
                listener(destination, data)
            }
        }
    }

    /**
     * 断开訂閱
     */
    fun untopic(vararg destinations: String) {
        destinations.forEach {
            proxy.untopic(it)
        }
    }

    /**
     * 整体断开所有连接
     */
    fun disconnect() {
        proxy.disconnect()
    }

}