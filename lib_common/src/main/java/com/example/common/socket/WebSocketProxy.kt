package com.example.common.socket

import cn.zhxu.okhttps.OkHttps
import cn.zhxu.stomp.Header
import cn.zhxu.stomp.Message
import cn.zhxu.stomp.Stomp
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.framework.utils.logWTF

/**
 * description
 * creator yan
 * https://ok.zhxu.cn/v4/stomp.html#maven
 */
class WebSocketProxy(private val socketUrl: String) {
    //socket服务器，同时设置心跳间隔为 10 秒
    private val stompClient by lazy { Stomp.over(OkHttps.webSocket(socketUrl).heatbeat(5, 10)) }

    companion object {
        internal var listener: (url: String?, data: Message?) -> Unit = { _, _ -> }

        fun setOnMessageListener(listener: (url: String?, data: Message?) -> Unit) {
            Companion.listener = listener
        }
    }

    /**
     * 建立连接
     * 连接成功后订阅对应的接收地址
     * 0：订单
     * 1：广告
     * 2：资金
     */
    private fun connect(vararg topicUrl: String) {
        //头部内容配置
        val headers = ArrayList<Header>()
//        headers.add(Header("Lang", language.get()))
//        headers.add(Header("Authorization", "Bearer ${token.get()}"))
        headers.add(Header("Client-Type", "mobile"))
        headers.add(Header("timeZone", "GMT+8"))
        stompClient
            .setOnConnected {
                // 服务器连接成功回调
                "Stomp connection opened $it".logWTF
                topic(*topicUrl)
            }
            .setOnDisconnected {
                // 连接已断开回调
                "Stomp connection closed $it".logWTF
            }
            .setOnError {
                // 错误监听（v2.4.1 新增）
                // 处理服务器发出的 ERROR 帧
                "Stomp Server connection error $it".logWTF
            }
            .setOnException {
                // 异常监听（v3.1.1 新增）
                // 处理服务器发出的 ERROR 帧
                "Stomp connection error $it".logWTF
            }
            .connect(headers)
    }

    /**
     *  订阅服务提供的topic
     */
    fun topic(vararg destinations: String) {
        if (!isLogin()) return
        if (!stompClient.isConnected) {
            connect(*destinations)
            return
        }

        destinations.forEach { destination ->
            //開始訂閱
            stompClient.subscribe(destination, null) {
                //得到消息负载
                val payload = it.payload
                "Received $payload".logWTF
                listener(destination, it)
            }
        }
    }

    /**
     * 断开訂閱
     */
    fun untopic(vararg destinations: String) {
        destinations.forEach {
            stompClient.untopic(it)
        }
    }

    /**
     * 关闭socket
     * 断开服务器连接，取消订阅
     * 登錄後主動調取一次，再次進入到對應訂閱頁面後，會重新建立連接和訂閱
     */
    fun disconnect() {
        stompClient.disconnect(true)
    }

}