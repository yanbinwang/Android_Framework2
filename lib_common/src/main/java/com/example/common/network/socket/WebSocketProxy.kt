package com.example.common.network.socket

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import cn.zhxu.okhttps.OkHttps
import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Header
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.framework.utils.logWTF
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 非订阅长连接类->websocket代理类
 * https://ok.zhxu.cn/v4/getstart.html#maven
 * https://blog.51cto.com/u_12682526/10303358
 */
class WebSocketProxy(private val socketUrl: String) {
    //回调监听
    private var listener: OnWebSocketProxyListener? = null
    //socket服务器，客户端每隔5秒向服务器发送一次PING消息，并期望服务器回复PONG消息的间隔5秒一次，
    //如果服务器或网络由于某些未知原因导致客户端未能正确收到PONG消息，客户端会容忍两次失败，当第三个5秒后还未收到服务器的任何消息时，则会触发SocketTimeoutException异常
    private val stomp by lazy { Stomp.over(OkHttps.webSocket(socketUrl).heatbeat(5, 5)) }
    //默认头部内容配置
    private val headers by lazy { mutableListOf(Header("Client-Type", "mobile"), Header("timeZone", "GMT+8")) }

    /**
     * 如果长连接此时正在连接，直接断开，1s后重新发起
     */
    fun connect(owner: LifecycleOwner, list: List<Header>? = headers) {
        if (isConnected()) {
            disconnect()
            owner.lifecycleScope.launch {
                delay(1000)
                connectNow(list)
            }
        } else {
            connectNow(list)
        }
    }

    private fun connectNow(list: List<Header>?) {
        stomp
            /**
             * 服务器连接成功回调
             */
            .setOnConnected {
                "Stomp connection opened $it".logWTF
//                topic(*topicUrl)
                listener?.onConnected(it)
            }
            /**
             * 连接已断开回调
             */
            .setOnDisconnected {
                "Stomp connection closed $it".logWTF
                listener?.onDisconnected(it)
            }
            /**
             * 错误监听（v2.4.1 新增）
             * 处理服务器发出的 ERROR 帧
             */
            .setOnError {
                "Stomp Server connection error $it".logWTF
                listener?.onError(it)
            }
            /**
             * 异常监听（v3.1.1 新增）
             * 处理服务器发出的 ERROR 帧
             */
            .setOnException {
                "Stomp connection error $it".logWTF
                listener?.onException(it)
            }
            .connect(list)
    }

    /**
     * 关闭socket
     * 断开服务器连接，取消订阅
     * 登錄後主動調取一次，再次進入到對應訂閱頁面後，會重新建立連接和訂閱
     */
    fun disconnect() {
        stomp.disconnect(true)
    }

    /**
     * 连接是否已建立
     */
    fun isConnected(): Boolean {
        return stomp.isConnected
    }

    /**
     *  订阅服务提供的topic
     */
    fun topic(destination: String, listener: (url: String?, data: Message?) -> Unit) {
        //開始訂閱
        stomp.subscribe(destination, null) {
            //得到消息负载
            val payload = it.payload
            "Received $payload".logWTF
            listener.invoke(destination, it)
        }
    }

    /**
     * 断开訂閱
     */
    fun untopic(destination: String) {
        stomp.untopic(destination)
    }

    /**
     * 设置监听
     */
    fun setOnWebSocketProxyListener(listener: OnWebSocketProxyListener) {
        this.listener = listener
    }

    /**
     * 自定义监听回调
     */
    interface OnWebSocketProxyListener {
        /**
         * 服务器连接成功回调
         */
        fun onConnected(onConnected: Stomp)

        /**
         * 连接已断开回调
         */
        fun onDisconnected(onConnected: WebSocket.Close)

        /**
         * 错误监听
         */
        fun onError(onConnected: Message)

        /**
         * 异常监听
         */
        fun onException(onConnected: Throwable)
    }

}