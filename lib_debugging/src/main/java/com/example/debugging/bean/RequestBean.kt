package com.example.debugging.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.example.common.utils.function.orNoData
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
    var type: String? = null,
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
     * 请求方式
     */
    val methodText get() = if ("SOCKET" == method) "$method $type" else method

    /**
     * 时间
     */
    val timeText get() = time.stamp2hms()

    /**
     * 标题
     */
    val titleText get() = url.orNoData()

}