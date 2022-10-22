package com.example.base.utils

import android.util.Log
import com.example.base.BuildConfig

/**
 * 日志输出类
 */
private const val TAG = "dota" // 默认的tag

object LogUtil {
    private const val debug = BuildConfig.ISDEBUG

    @JvmStatic
    fun v(tag: String = TAG, msg: String) = run { if (debug) Log.v(tag, msg) }

    @JvmStatic
    fun d(tag: String = TAG, msg: String) = run { if (debug) Log.d(tag, msg) }

    @JvmStatic
    fun i(tag: String = TAG, msg: String) = run { if (debug) Log.i(tag, msg) }

    @JvmStatic
    fun w(tag: String = TAG, msg: String) = run { if (debug) Log.w(tag, msg) }

    @JvmStatic
    fun e(tag: String = TAG, msg: String) = run { if (debug) Log.e(tag, msg) }

    @JvmStatic
    fun wtf(msg: String) = run { if (debug) Log.println(Log.ASSERT, TAG, msg) }

    @JvmStatic
    fun wtf(tag: String = TAG, msg: String) = run { if (debug) Log.println(Log.ASSERT, tag, msg) }

}

val String?.logV: Unit get() { if (this != null) LogUtil.v(msg = this) }

fun String?.logV(tag: String = TAG) = run { if (this != null) LogUtil.v(tag, this) }

val String?.logD: Unit get() { if (this != null) LogUtil.d(msg = this) }

fun String?.logD(tag: String = TAG) = run { if (this != null) LogUtil.d(tag, this) }

val String?.logI: Unit get() { if (this != null) LogUtil.i(msg = this) }

fun String?.logI(tag: String = TAG) = run { if (this != null) LogUtil.i(tag, this) }

val String?.logW: Unit get() { if (this != null) LogUtil.w(msg = this) }

fun String?.logW(tag: String = TAG) = run { if (this != null) LogUtil.w(tag, this) }

val String?.logE: Unit get() { if (this != null) LogUtil.e(msg = this) }

fun String?.logE(tag: String = TAG) = run { if (this != null) LogUtil.e(tag, this) }

val String?.logWTF: Unit get() { if (this != null) LogUtil.wtf(msg = this) }

fun String?.logWTF(tag: String = TAG) = run { if (this != null) LogUtil.wtf(tag, this) }