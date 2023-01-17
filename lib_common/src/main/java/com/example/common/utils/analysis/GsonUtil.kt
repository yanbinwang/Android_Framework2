package com.example.common.utils.analysis

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * author:wyb
 * 对象转换类
 */
object GsonUtil {
    private val gson by lazy {
        GsonBuilder().setLenient()//json宽松,针对json格式不规范
            .disableHtmlEscaping()//防止特殊字符出现乱码
            .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter()).create()
    }

    @JvmStatic
    fun <T> jsonToObj(json: String, className: Class<T>): T? {
        var ret: T? = null
        try {
            ret = gson.fromJson(json, className)
        } catch (_: Exception) {
        }
        return ret
    }

    @JvmStatic
    fun <T> jsonToList(json: String): List<T>? {
        var ret: List<T>? = null
        try {
            ret = gson.fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type)
        } catch (_: Exception) {
        }
        return ret
    }

//    @JvmStatic
//    fun <T> jsonToList(array: JsonArray): List<T>? {
//        var ret: List<T>? = null
//        try {
//            ret = gson.fromJson<List<T>>(array, object : TypeToken<List<T>>() {}.type)
//        } catch (_: Exception) {
//        }
//        return ret
//    }

    @JvmStatic
    fun <T> jsonToList(array: JsonArray, type: Type): List<T>? {
        var ret: List<T>? = null
        try {
            ret = gson.fromJson<List<T>>(array, type)
        } catch (_: Exception) {
        }
        return ret
    }

    @JvmStatic
    fun objToJson(obj: Any): String? {
        var ret: String? = null
        try {
            ret = gson.toJson(obj)
        } catch (_: Exception) {
        }
        return ret
    }

}