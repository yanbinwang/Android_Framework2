package com.example.debugging.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.example.common.utils.function.color
import com.example.common.utils.function.orNoData
import com.example.debugging.R
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.orZero
import kotlinx.parcelize.Parcelize

/**
 * 请求内容记录
 */
@SuppressLint("SimpleDateFormat")
@Parcelize
data class RequestBean(
    var url: String? = null,
    var method: String? = null,
    var header: String? = null,
    var params: String? = null,
    var time: Long? = null,
    var code: Int? = null,
    var body: String? = null
) : Parcelable {

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
    val timeText get() = "yyyy/MM/dd HH:mm:ss".convert(time.orZero)

    /**
     * 标题
     */
    val titleText get() = url.orNoData()

}