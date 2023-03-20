package com.example.common.event

/**
 * socket引發的一些頁面的刷新
 */
object SocketEventCode {
    //socket订单改变
    val EVENT_SOCKET_TYPE_DEAL = Code<String>()

    //socket广告改变
    val EVENT_SOCKET_TYPE_ADVERTISE = Code<String>()

    //socket资金改变
    val EVENT_SOCKET_TYPE_FUNDS = Code<String>()
}

