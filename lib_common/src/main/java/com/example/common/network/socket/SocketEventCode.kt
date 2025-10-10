package com.example.common.network.socket

import com.example.common.event.Code

/**
 * socket广播
 */
object SocketEventCode {
    //socket订单改变
    val EVENT_SOCKET_DEAL = Code<String>()

    //socket广告改变
    val EVENT_SOCKET_ADVERTISE = Code<String>()

    //socket资金改变
    val EVENT_SOCKET_FUNDS = Code<String>()
}

