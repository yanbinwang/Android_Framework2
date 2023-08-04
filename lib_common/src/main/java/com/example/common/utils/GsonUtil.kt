package com.example.common.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
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
    fun <T> jsonToList(json: String, type: Type): List<T>? {
        var ret: List<T>? = null
        try {
            ret = gson.fromJson<List<T>>(json, type)
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

class BooleanTypeAdapter : TypeAdapter<Boolean>() {

    @Throws(IOException::class)
    override fun write(writer: JsonWriter, value: Boolean?) {
        writer.value(value.toString())
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): Boolean? {
        return try {
            val value = reader.nextString()
            "Y" == value || "1" == value || "true" == value
        } catch (e: NullPointerException) {
            false
        }
    }

}