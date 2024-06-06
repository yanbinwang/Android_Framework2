package com.example.mvvm.utils.socket

import cn.zhxu.okhttps.OkHttps
import cn.zhxu.okhttps.WebSocket
import cn.zhxu.stomp.Header
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.logWTF

/**
 * 非订阅长连接类
 */
class WebSocketProxy(private val socketUrl: String) {
    //回调监听
    private var listener: OnWebSocketProxyListener? = null
    //socket服务器，同时设置心跳间隔为 10 秒
    private val stompClient by lazy { Stomp.over(OkHttps.webSocket(socketUrl).heatbeat(5, 10)) }
    //默认头部内容配置
    private val headers by lazy { mutableListOf(Header("Client-Type", "mobile"), Header("timeZone", "GMT+8")) }

    /**
     * 如果长连接此时正在连接，直接断开，1s后重新发起
     */
    fun connect(list: List<Header>? = headers) {
        if (isConnected()) {
            disconnect()
            schedule({ connectNow(list) })
        } else {
            connectNow(list)
        }
    }

    private fun connectNow(headers: List<Header>?) {
        stompClient
            .setOnConnected {
                // 服务器连接成功回调
                "Stomp connection opened $it".logWTF
//                topic(*topicUrl)
                listener?.onConnected(it)
            }
            .setOnDisconnected {
                // 连接已断开回调
                "Stomp connection closed $it".logWTF
                listener?.onDisconnected(it)
            }
            .setOnError {
                // 错误监听（v2.4.1 新增）
                // 处理服务器发出的 ERROR 帧
                "Stomp Server connection error $it".logWTF
                listener?.onError(it)
            }
            .setOnException {
                // 异常监听（v3.1.1 新增）
                // 处理服务器发出的 ERROR 帧
                "Stomp connection error $it".logWTF
                listener?.onException(it)
            }
            .connect(headers)
    }

    /**
     * 关闭socket
     * 断开服务器连接，取消订阅
     * 登錄後主動調取一次，再次進入到對應訂閱頁面後，會重新建立連接和訂閱
     */
    fun disconnect() {
        stompClient.disconnect(true)
    }

    /**
     * 连接是否已建立
     */
    fun isConnected(): Boolean {
        return stompClient.isConnected
    }

    /**
     * 设置监听
     */
    fun setOnWebSocketProxyListener(listener: OnWebSocketProxyListener) {
        this.listener = listener
    }

    /**
     *  订阅服务提供的topic
     */
    fun topic(destination: String, listener: (url: String?, data: Message?) -> Unit) {
        //開始訂閱
        stompClient.subscribe(destination, null) {
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
        stompClient.untopic(destination)
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