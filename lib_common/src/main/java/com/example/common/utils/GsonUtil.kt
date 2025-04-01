package com.example.common.utils

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.ParameterizedType
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
     * 对象转json字符串
     */
    @JvmStatic
    fun objToJson(obj: Any): String? {
        var ret: String? = null
        try {
            ret = gson.toJson(obj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }

    /**
     * json字符串转对象
     * val testBean = "{\"author\":\"啊啊啊啊\",\"genre\":\"2 2 2 2 2 2\",\"title\":\"十大大大大1111\"}".toObj(Book::class.java)
     */
    @JvmStatic
    fun <T> jsonToObj(json: String, clazz: Class<T>): T? {
        var ret: T? = null
        try {
            ret = gson.fromJson(json, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }

    /**
     * 如果class内部运用了泛型，则传type，不然会被擦除
     * val type = getType(List::class.java, List::class.java)
     */
    @JvmStatic
    fun <T> jsonToObj(json: String, type: Type): T? {
        var ret: T? = null
        try {
            ret = gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }

    /**
     * json字符串转集合
     * 由于类型擦除，解析器无法在运行时获取真实类型 T
     * 直接传T获取会报com.google.gson.internal.LinkedTreeMap cannot be cast to object
     * 故而直接把T的class传入，让解析器能够识别，并且重新转换成一个list
     * val testList = "[{\"author\":\"n11111\",\"genre\":\"11111\",\"title\":\"The Fng11111\"},{\"author\":\"J.D. Sa222\",\"genre\":\"Fn22222\",\"title\":\"Thye22222\"}]".toList(Book::class.java)
     */
    @JvmStatic
    fun <T> jsonToList(json: String, clazz: Class<T>): List<T>? {
        var ret: List<T>? = null
        try {
            val type = getType(List::class.java, clazz)
            ret = gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }

    /**
     * 获取type类型
     * //List<String>的type为
     *  val type = getType(List::class.java,String::class.java)
     * //List<List<String>>的type为
     *  val type = getType(List::class.java,getType(List::class.java,String::class.java))
     * //Map<Int,String>的type为
     * val type = getType(List::class.java,Int::class.java,String::class.java)
     * //Map<String,List<String>>的类型为
     * val type = getType(Map::class.java,String::class.java, getType(List::class.java,String::class.java))
     */
    @JvmStatic
    fun getType(raw: Class<*>, vararg args: Type) = object : ParameterizedType {

        override fun getRawType(): Type = raw

        override fun getActualTypeArguments(): Array<out Type> = args

        override fun getOwnerType(): Type? = null

    }

    /**
     * 定义服务器返回特定字段转为布尔
     */
    private class BooleanTypeAdapter : TypeAdapter<Boolean>() {

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

}

/**
 * 对象转json
 */
fun Any?.toJson(): String? {
    if (this == null) return null
    return GsonUtil.objToJson(this)
}

/**
 * 将json转换为对象
 */
fun <T> String?.toObj(clazz: Class<T>): T? {
    if (this == null) return null
    return GsonUtil.jsonToObj(this, clazz)
}

fun <T> String?.toObj(type: Type): T? {
    if (this == null) return null
    return GsonUtil.jsonToObj(this, type)
}

/**
 * 后端请求如果data是JsonArray的话，使用该方法得到一个集合
 */
fun <T> String?.toList(clazz: Class<T>): List<T>? {
    if (this == null) return null
    return GsonUtil.jsonToList(this, clazz)
}