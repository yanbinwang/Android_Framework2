package com.example.common.utils

import android.os.Parcelable
import com.example.common.utils.MmkvUtil.decodeBool
import com.example.common.utils.MmkvUtil.decodeBytes
import com.example.common.utils.MmkvUtil.decodeDouble
import com.example.common.utils.MmkvUtil.decodeFloat
import com.example.common.utils.MmkvUtil.decodeInt
import com.example.common.utils.MmkvUtil.decodeLong
import com.example.common.utils.MmkvUtil.decodeParcelable
import com.example.common.utils.MmkvUtil.decodeString
import com.example.common.utils.MmkvUtil.encode
import com.example.common.utils.MmkvUtil.removeValueForKey
import com.tencent.mmkv.MMKV

/**
 *  Created by wangyanbin
 *  应用存储工具类
 */
object MmkvUtil {
    private val mmkv by lazy { MMKV.defaultMMKV() }

    fun encode(key: String, value: Boolean) = mmkv.encode(key, value)
    fun encode(key: String, value: Int) = mmkv.encode(key, value)
    fun encode(key: String, value: Long) = mmkv.encode(key, value)
    fun encode(key: String, value: Float) = mmkv.encode(key, value)
    fun encode(key: String, value: Double) = mmkv.encode(key, value)
    fun encode(key: String, value: String) = mmkv.encode(key, value)
    fun encode(key: String, value: ByteArray) = mmkv.encode(key, value)
    fun <T : Parcelable> encode(key: String, value: T?) = mmkv.encode(key, value)

    fun decodeBool(key: String, value: Boolean = false) = mmkv.decodeBool(key, value)
    fun decodeInt(key: String, value: Int = 0) = mmkv.decodeInt(key, value)
    fun decodeLong(key: String, value: Long = 0) = mmkv.decodeLong(key, value)
    fun decodeFloat(key: String, value: Float = 0f) = mmkv.decodeFloat(key, value)
    fun decodeDouble(key: String, value: Double = 0.0) = mmkv.decodeDouble(key, value)
    fun decodeString(key: String, value: String = "") = mmkv.decodeString(key, value)
    fun decodeBytes(key: String, value: ByteArray = byteArrayOf()) = mmkv.decodeBytes(key, value)
    fun <T : Parcelable> decodeParcelable(label: String, tClass: Class<T>, value: T? = null) = mmkv.decodeParcelable(label, tClass, value)

    fun removeValueForKey(label: String) = mmkv.removeValueForKey(label)
}

abstract class BaseDataCache(private val key: String) {
    fun del() = removeValueForKey(key)
}

class DataBooleanCache(private val key: String, private val defaultValue: Boolean = false) : BaseDataCache(key) {
    fun get() = decodeBool(key, defaultValue)
    fun set(value: Boolean) = encode(key, value)
}

class DataIntCache(private val key: String, private val defaultValue: Int = 0) : BaseDataCache(key) {
    fun get() = decodeInt(key, defaultValue)
    fun set(value: Int) = encode(key, value)
}

class DataLongCache(private val key: String, private val defaultValue: Long = 0) : BaseDataCache(key) {
    fun get() = decodeLong(key, defaultValue)
    fun set(value: Long) = encode(key, value)
}

class DataFloatCache(private val key: String, private val defaultValue: Float = 0f) : BaseDataCache(key) {
    fun get() = decodeFloat(key, defaultValue)
    fun set(value: Float) = encode(key, value)
}

class DataDoubleCache(private val key: String, private val defaultValue: Double = 0.0) : BaseDataCache(key) {
    fun get() = decodeDouble(key, defaultValue)
    fun set(value: Double) = encode(key, value)
}

class DataStringCache(private val key: String, private val defaultValue: String = "") : BaseDataCache(key) {
    fun get() = decodeString(key, defaultValue)
    fun set(value: String) = encode(key, value)
}

class DataBytesCache(private val key: String, private val defaultValue: ByteArray = byteArrayOf()) : BaseDataCache(key) {
    fun get() = decodeBytes(key, defaultValue)
    fun set(value: ByteArray) = encode(key, value)
}

class DataParcelableCache<T : Parcelable>(private val key: String, private val clazz: Class<T>, private val value: T? = null) : BaseDataCache(key) {
    fun get() = decodeParcelable(key, clazz, value)//可能存在获取到空对象的情况，切记写个?：UserBean()，源码内部会在不为null的时候把clazz打成bytes存到mmkv里，先取的时候如果为null则返回默认值
    fun set(value: T) = encode(key, value)
}