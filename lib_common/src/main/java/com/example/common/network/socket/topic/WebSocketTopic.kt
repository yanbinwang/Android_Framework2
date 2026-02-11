package com.example.common.network.socket.topic

import androidx.lifecycle.LifecycleOwner
import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.common.network.socket.WebSocketProxy
import com.example.common.utils.helper.AccountHelper.isLogin
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 如果页面是需要订阅多个地址的，实现当前页面
 */
class WebSocketTopic(private val socketUrl: String) {
    private val proxy by lazy { WebSocketProxy(socketUrl) } // 代理类
    private val wssList by lazy { CopyOnWriteArrayList<String>() } // 页面所订阅的所有长连接集合

    companion object {
        internal var proxyListener: (url: String?, data: Message?) -> Unit = { _, _ -> }
        internal var messageListener: (data: String) -> Unit = { _ -> }

        /**
         * 再BaseApplication中实现回调，通过EvenBus分发对应接受的消息
         * 具备唯一性，只有多个wss地址订阅的时候，需要在Application实现此监听，然后全局发布广播（EvenBus）
         */
        @JvmStatic
        fun setOnProxyListener(listener: (url: String?, data: Message?) -> Unit) {
            proxyListener = listener
        }

        /**
         * 单一无需订阅时调取
         */
        fun setOnMessageListener(listener: (String) -> Unit) {
            messageListener = listener
        }
    }

    init {
        // 代理类的回调监听，一旦地址连接成功，自动订阅
        proxy.setOnWebSocketProxyListener(object : WebSocketProxy.OnWebSocketProxyListener {
            override fun onConnected(onConnected: Stomp) {
                topicNow()
            }

            override fun onDisconnected(onDisconnected: WebSocket.Close) {
            }

            override fun onError(onError: Message) {
            }

            override fun onException(onException: Throwable) {
            }
        })
        // 全局消息监听
        proxy.setOnWebSocketMessageListener { rawMsg ->
            if (!isLogin() || !proxy.isConnected() || rawMsg.isNullOrEmpty()) return@setOnWebSocketMessageListener
            messageListener(rawMsg)
        }
    }

    /**
     *  建立websocket连接，批量订阅务提供的wss地址
     */
    fun topic(owner: LifecycleOwner, vararg destinations: String) {
        // 未登录不订阅
        if (!isLogin()) return
        // 未连接先不订阅，先做地址连接（proxy.connect()），连接成功后会在onConnected（）回调监听中订阅
        wssList.clear()
        // 去重，避免相同主题重复订阅
        wssList.addAll(destinations.toList().distinct())
        // 校验是否连接
        if (!proxy.isConnected()) {
            proxy.connect(owner)
            return
        }
        // 开始批量订阅wss地址
        topicNow()
    }

    /**
     *  订阅服务提供的wss地址
     */
    private fun topicNow() {
        wssList.forEach {
            proxy.topic(it) { _: String?, data: Message? ->
                proxyListener(it, data)
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
     * 发送消息
     */
    fun sendTo(destination: String, data: String) {
        // 未登录/不包含地址不发送
        if (!isLogin() || !wssList.contains(destination) || !proxy.isConnected()) return
        proxy.sendTo(destination, data)
    }

    /**
     * 整体断开所有连接
     */
    fun disconnect() {
        proxy.disconnect()
    }

}