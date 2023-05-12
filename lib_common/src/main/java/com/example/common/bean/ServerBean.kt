package com.example.common.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 服务器请求类
 */
@Parcelize
data class ServerBean(
    var server: String = "",
    var port: Int = 0,
    var path: String = "",
    var name: String = "",
    var https: Boolean = false
) : Parcelable {

    /**
     * 获取完整的请求地址
     */
    fun getUrl(): String {
        val builder = StringBuilder()
        builder.apply {
            if (https) {
                append("https://")
            } else {
                append("http://")
            }
            append(server)
            if (port > 0) {
                append(":")
                append(port)
            }
            if (path.isNotEmpty()) {
                append("/")
                append(path)
            }
            append("/")
        }
        return builder.toString()
    }

    /**
     * 是否是https请求
     */
    fun https(): ServerBean {
        https = true
        return this
    }

    fun http(): ServerBean {
        https = false
        return this
    }

}