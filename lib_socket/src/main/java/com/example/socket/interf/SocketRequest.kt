package com.example.common.socket.interf

/**
 * @description socket走頁面訂閱形式，故而檢測到有註解的才會做處理
 * @author yan
 */
annotation class SocketRequest(vararg val value: String)