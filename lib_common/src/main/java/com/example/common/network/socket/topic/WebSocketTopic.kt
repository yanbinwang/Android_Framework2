package com.example.common.network.socket.topic

import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.common.network.socket.WebSocketProxy
import com.example.common.utils.helper.AccountHelper.isLogin

/**
 * 如果页面是需要订阅多个地址的，实现当前页面
 */
class WebSocketTopic(private val socketUrl: String) {
    private val proxy by lazy { WebSocketProxy(socketUrl) }//代理类
    private val list by lazy { ArrayList<String>() }//页面所订阅的所有长连接集合

    companion object {
        internal var listener: (url: String?, data: Message?) -> Unit = { _, _ -> }

        /**
         * 再baseapplication中实现回调，通过evenbus分发对应接受的消息
         * 具备唯一性，只有多个wss地址订阅的时候，需要在application实现此监听，然后全局发布广播（EvenBus）
         */
        @JvmStatic
        fun setOnMessageListener(listener: (url: String?, data: Message?) -> Unit) {
            Companion.listener = listener
        }
    }

    init {
        //代理类的回调监听，一旦地址连接成功，自信订阅
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
     *  建立websocket连接，批量订阅务提供的wss地址
     */
    fun topic(vararg destinations: String) {
        //未登录不订阅
        if (!isLogin()) return
        //未连接先不订阅，先做地址连接（proxy.connect()），连接成功后会在onConnected（）回调监听中订阅
        list.clear()
        list.addAll(destinations.toList())
        if (!proxy.isConnected()) {
            proxy.connect()
            return
        }
        //开始批量订阅wss地址
        topicNow()
    }

    /**
     *  订阅服务提供的wss地址
     */
    private fun topicNow() {
        list.forEach {
            proxy.topic(it) { _: String?, data: Message? ->
                listener(it, data)
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