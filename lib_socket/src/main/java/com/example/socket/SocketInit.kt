package com.example.socket

/**
 * @description socket监听
 * @author yan
 */
object SocketInit {
    internal var listener: (url: String?, payload: String?) -> Unit = { _, _ -> }

    fun setOnMessageListener(listener: (url: String?, payload: String?) -> Unit) {
        this.listener = listener
    }

}