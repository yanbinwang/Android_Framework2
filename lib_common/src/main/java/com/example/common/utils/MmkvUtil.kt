package com.example.common.utils

import android.os.Parcelable
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
    fun decodeBool(key: String) = mmkv.decodeBool(key, false)

    @JvmStatic
    fun decodeString(key: String) = mmkv.decodeString(key, "")

    @JvmStatic
    fun <T : Parcelable> decodeParcelable(label: String, tClass: Class<T>) = mmkv.decodeParcelable(label, tClass)

    @JvmStatic
    fun removeValueForKey(label: String) = mmkv.removeValueForKey(label)

}