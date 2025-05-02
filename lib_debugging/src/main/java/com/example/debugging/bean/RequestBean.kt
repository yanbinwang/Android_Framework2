package com.example.debugging.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.example.common.utils.function.color
import com.example.common.utils.function.orNoData
import com.example.debugging.R
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat

/**
 * 请求内容记录
 */
@SuppressLint("SimpleDateFormat")
@Parcelize
data class RequestBean(
    var url: String? = null,
    var method: String? = null,
    var header: String? = null,
    var body: String? = null,
    var time: Long? = null,
    var code: Int? = null,
    var response: String? = null
) : Parcelable {

    companion object {
        private val sdf by lazy { SimpleDateFormat() }

        /**
         * 输出 yyyy/MM/dd hh:mm:ss
         */
        fun Long?.stamp2hms(default: String = "--"): String {
            if (this == null) return default
            if (this <= 0) return default
            sdf.applyPattern("yyyy/MM/dd HH:mm:ss")
            return sdf.format(this)
        }
    }

    /**
     * 请求方式颜色
     * color(R.color.textRed)
     */
    val methodColorRes get() = when (method) {
        "POST" -> color(R.color.http_post)
        "GET" -> color(R.color.http_get)
        "PUT" -> color(R.color.http_put)
        "PUSH" -> color(R.color.http_push)
        "SOCKET" -> color(R.color.http_socket)
        "PATCH" -> color(R.color.http_patch)
        "DELETE" -> color(R.color.http_delete)
        else -> color(R.color.http_other)
    }

    /**
     * 时间
     */
    val timeText get() = time.stamp2hms()

    /**
     * 标题
     */
    val titleText get() = url.orNoData()

}