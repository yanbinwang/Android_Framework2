package com.example.socket

import cn.zhxu.okhttps.OkHttps
import cn.zhxu.stomp.Header
import cn.zhxu.stomp.Stomp
import com.example.framework.utils.logWTF

/**
 * @description socket連接類
 * https://ok.zhxu.cn/v4/stomp.html#maven
 * @author yan
 */
class WebSocketProxy(private val socketUrl: String) {
    //socket服务器，同时设置心跳间隔为 20 秒
    private val stompClient by lazy { Stomp.over(OkHttps.webSocket(socketUrl).heatbeat(5, 10)) }

    /**
     * 建立连接
     * 连接成功后订阅对应的接收地址
     * 0：订单
     * 1：广告
     * 2：资金
     */
    private fun connect(vararg value: String) {
        //头部内容配置
        val headers = ArrayList<Header>()
//        headers.add(Header("Lang", language.get()))
//        headers.add(Header("Authorization", "Bearer ${token.get()}"))
        headers.add(Header("Client-Type", "mobile"))
        headers.add(Header("timeZone", "GMT+8"))
        stompClient
            .setOnConnected {
                //服务器连接成功回调
                "Stomp connection opened $it".logWTF
                topic(*value)
            }
            .setOnDisconnected {
                //连接已断开回调
                "Stomp connection closed $it".logWTF
            }
            .setOnError {
                //错误监听,处理服务器发出的 ERROR 帧
                "Stomp Server connection error $it".logWTF
            }
            .setOnException {
                //异常监听,处理服务器发出的 ERROR 帧
                "Stomp connection error $it".logWTF
            }
            .connect(headers)
    }

    /**
     *  订阅服务提供的topic
     */
    fun topic(vararg value: String) {
        if (!stompClient.isConnected) {
            connect(*value)
            return
        }
        value.forEach { url ->
            //開始訂閱
            stompClient.subscribe(url, null) {
                //得到消息负载
                val payload = it.payload
//                "Received ${it.toJsonString()}".logWTF
                SocketInit.listener(url, payload)
            }
        }
    }

    /**
     * 断开訂閱
     */
    fun untopic(vararg value: String) {
        value.forEach {
            stompClient.untopic(it)
        }
    }

    /**
     * 关闭socket
     * 断开服务器连接，取消订阅
     */
    fun disconnect() {
        stompClient.disconnect()
    }

}