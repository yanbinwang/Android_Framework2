package com.example.socket.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 服务器请求类
 */
@Parcelize
data class SocketBean(
    var server: String = "",
    var path: String = "",
    var name: String = ""
) : Parcelable {

    /**
     * 获取完整的请求地址
     */
    fun getUrl(): String {
        val builder = StringBuilder()
        builder.apply {
            append("wss://")
            append(server)
            if (path.isNotEmpty()) {
                append("/")
                append(path)
            }
            append("/websocket")
        }
        return builder.toString()
    }

}