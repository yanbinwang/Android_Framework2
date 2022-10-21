package com.example.base.utils

import android.util.Log
import com.example.base.BuildConfig

/**
 * 日志输出类
 */
object LogUtil {
    private const val debug = BuildConfig.ISDEBUG
    private const val TAG = "dota" // 默认的tag

    @JvmStatic
    fun v(msg: String) = run { if (debug) Log.v(TAG, msg) }

    @JvmStatic
    fun v(tag: String, msg: String) = run { if (debug) Log.v(tag, msg) }

    @JvmStatic
    fun d(msg: String) = run { if (debug) Log.d(TAG, msg) }

    @JvmStatic
    fun d(tag: String, msg: String) = run { if (debug) Log.d(tag, msg) }

    @JvmStatic
    fun i(msg: String) = run { if (debug) Log.i(TAG, msg) }

    @JvmStatic
    fun i(tag: String, msg: String) = run { if (debug) Log.i(tag, msg) }

    @JvmStatic
    fun w(msg: String) = run { if (debug) Log.w(TAG, msg) }

    @JvmStatic
    fun w(tag: String, msg: String) = run { if (debug) Log.w(tag, msg) }

    @JvmStatic
    fun e(msg: String) = run { if (debug) Log.e(TAG, msg) }

    @JvmStatic
    fun e(tag: String, msg: String) = run { if (debug) Log.e(tag, msg) }

    @JvmStatic
    fun wtf(msg: String) = run { if (debug) Log.println(Log.ASSERT, TAG, msg) }

    @JvmStatic
    fun wtf(tag: String, msg: String) = run { if (debug) Log.println(Log.ASSERT, tag, msg) }

}

val String?.logV: Unit get() { if (this != null) LogUtil.v(this) }

val String?.logD: Unit get() { if (this != null) LogUtil.d(this) }

val String?.logW: Unit get() { if (this != null) LogUtil.w(this) }

val String?.logE: Unit get() { if (this != null) LogUtil.e(this) }

val String?.logWTF: Unit get() { if (this != null) LogUtil.wtf(this) }