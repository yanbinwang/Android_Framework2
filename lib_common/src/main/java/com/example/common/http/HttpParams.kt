package com.example.common.http

import android.text.TextUtils
import java.util.*

class HttpParams {
    private var map: MutableMap<String, String> = HashMap()

    fun append(key: String, value: String?): HttpParams {
        if (!TextUtils.isEmpty(value)) {
            if (value != null) {
                map[key] = value
            }
        }
        return this
    }

    fun setMap(map: MutableMap<String, String>): HttpParams {
        this.map = map
        return this
    }

    fun getParams(): Map<String, String> {
        return map
    }

//    //请求参数加密
//    fun getParams(timestamp: String?): Map<String, String> {
//        map["timestamp"] = timestamp!!
//        map["network"] = NetWorkUtil.getAPNType()
//        map["sign"] = SecurityUtil.doSign(map)
//        map = SecurityUtil.sortParams(map)
//        if (SecurityUtil.needEncrypt()) {
//            val param = SecurityUtil.doEncrypt(map)
//            map.clear()
//            map["param"] = param
//            map["timestamp"] = timestamp
//        }
//        return map
//    }

}
