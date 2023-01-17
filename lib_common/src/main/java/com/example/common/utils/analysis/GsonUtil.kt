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

    /**
     * json字符串转对象
     */
    fun <T> jsonToObj(json: String, className: Class<T>): T? {
        var ret: T? = null
        try {
            ret = gson.fromJson(json, className)
        } catch (_: Exception) {
        }
        return ret
    }

    /**
     * json字符串转集合
     */
    fun <T> jsonToList(json: String): List<T>? {
        var ret: List<T>? = null
        try {
            ret = gson.fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type)
        } catch (_: Exception) {
        }
        return ret
    }

    /**
     * JsonArray转集合
     * 由于类型擦除，解析器无法在运行时获取真实类型 T
     * 直接传T获取会报com.google.gson.internal.LinkedTreeMap cannot be cast to object
     * 故而直接把T的class传入，让解析器能够识别，并且重新转换成一个list
     */
    fun <T> jsonToList(array: JsonArray, clazz: Class<T>): List<T> {
        val ret = ArrayList<T>()
        try {
            array.forEach { ret.add(gson.fromJson(it, clazz)) }
        } catch (_: Exception) {
        }
        return ret
    }

    /**
     * JsonArray转集合
     * 指定type->object : TypeToken<List<XXXX>>() {}.type
     */
    fun <T> jsonToList(array: JsonArray, type: Type): List<T>? {
        var ret: List<T>? = null
        try {
            ret = gson.fromJson<List<T>>(array, type)
        } catch (_: Exception) {
        }
        return ret
    }

    /**
     * 对象转json字符串
     */
    fun objToJson(obj: Any): String? {
        var ret: String? = null
        try {
            ret = gson.toJson(obj)
        } catch (_: Exception) {
        }
        return ret
    }

}