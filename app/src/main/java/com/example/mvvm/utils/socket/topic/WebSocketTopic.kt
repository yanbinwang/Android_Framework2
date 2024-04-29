package com.example.mvvm.utils.socket.topic

import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.mvvm.utils.socket.WebSocketProxy

/**
 * 如果页面是需要订阅多个地址的，实现当前页面
 */
class WebSocketTopic(private val url: String) {
    private val proxy by lazy { WebSocketProxy(url) }
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
                topic()
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
        list.clear()
        destinations.forEach { list.add(it) }
        proxy.connect()
    }

    /**
     *  订阅服务提供的topic
     */
    private fun topic() {
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