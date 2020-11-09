package com.example.common.http.repository

import android.text.TextUtils
import java.util.*

/**
 * 请求参数封装类
 */
class HttpParams {
    var map: MutableMap<String, String> = HashMap()//请求的参数map

    //参数的添加
    fun append(key: String, value: String?): HttpParams {
        if (!TextUtils.isEmpty(value)) {
            if (value != null) {
                map[key] = value
            }
        }
        return this
    }

//    //参数加密
//    fun signParams(timestamp: String?): Map<String, String> {
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
