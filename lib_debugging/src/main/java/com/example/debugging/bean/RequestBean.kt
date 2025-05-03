package com.example.debugging.bean

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

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is RequestBean) {
            return false
        }
        return url == other.url &&
                method == other.method &&
                header == other.header &&
                params == other.params &&
                time == other.time &&
                code == other.code &&
                body == other.body
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + body.hashCode()
        return result
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
    val timeText get() = "yyyy/MM/dd HH:mm:ss".convert(time.orZero)

    /**
     * 标题
     */
    val titleText get() = url.orNoData()

}