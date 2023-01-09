package com.example.common.utils

import android.os.Parcelable
import com.example.common.utils.MmkvUtil.decodeBool
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

    @JvmStatic
    fun encode(key: String, value: Boolean) = mmkv.encode(key, value)

    @JvmStatic
    fun encode(key: String, value: String) = mmkv.encode(key, value)

    @JvmStatic
    fun <T : Parcelable> encode(key: String, value: T?) = mmkv.encode(key, value)

    @JvmStatic
    fun decodeBool(key: String, value: Boolean = false) = mmkv.decodeBool(key, value)

    @JvmStatic
    fun decodeString(key: String, value: String = "") = mmkv.decodeString(key, value)

    @JvmStatic
    fun <T : Parcelable> decodeParcelable(label: String, tClass: Class<T>) = mmkv.decodeParcelable(label, tClass)

    @JvmStatic
    fun removeValueForKey(label: String) = mmkv.removeValueForKey(label)

}

class DataBooleanCacheUtil(private val key: String, private val value: Boolean = false) {
    fun get() = decodeBool(key, value)

    fun set(value: Boolean) = encode(key, value)

    fun del() = removeValueForKey(key)
}

class DataStringCacheUtil(private val key: String, private val value: String = "") {
    fun get() = decodeString(key, value)

    fun set(value: String) = encode(key, value)

    fun del() = removeValueForKey(key)
}

class DataCacheUtil<T : Parcelable>(private val key: String, private val clazz: Class<T>) {
    fun get() = decodeParcelable(key, clazz)

    fun set(value: T) = encode(key, value)

    fun del() = removeValueForKey(key)
}