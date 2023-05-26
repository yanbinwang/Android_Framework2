package com.example.framework.utils

import android.util.Log
import com.example.framework.utils.function.value.isDebug

/**
 * 日志输出类
 */
private const val TAG = "dota" // 默认的tag

object LogUtil {
    fun v(tag: String = TAG, msg: String) = run { if (isDebug) Log.v(tag, msg) }

    fun d(tag: String = TAG, msg: String) = run { if (isDebug) Log.d(tag, msg) }

    fun i(tag: String = TAG, msg: String) = run { if (isDebug) Log.i(tag, msg) }

    fun w(tag: String = TAG, msg: String) = run { if (isDebug) Log.w(tag, msg) }

    fun e(tag: String = TAG, msg: String) = run { if (isDebug) Log.e(tag, msg) }

    fun wtf(msg: String) = run { if (isDebug) Log.println(Log.ASSERT, TAG, msg) }

    fun wtf(tag: String = TAG, msg: String) = run { if (isDebug) Log.println(Log.ASSERT, tag, msg) }
}

val String?.logV: Unit get() { if (this != null) LogUtil.v(msg = this) }

val String?.logD: Unit get() { if (this != null) LogUtil.d(msg = this) }

val String?.logI: Unit get() { if (this != null) LogUtil.i(msg = this) }

val String?.logW: Unit get() { if (this != null) LogUtil.w(msg = this) }

val String?.logE: Unit get() { if (this != null) LogUtil.e(msg = this) }

val String?.logWTF: Unit get() { if (this != null) LogUtil.wtf(msg = this) }

val Throwable?.logE: Unit get() { this?.toString()?.logE }

val Throwable?.logWTF: Unit get() { this?.toString()?.logWTF }

fun String?.logV(tag: String = TAG) = run { if (this != null) LogUtil.v(tag, this) }

fun String?.logD(tag: String = TAG) = run { if (this != null) LogUtil.d(tag, this) }

fun String?.logI(tag: String = TAG) = run { if (this != null) LogUtil.i(tag, this) }

fun String?.logW(tag: String = TAG) = run { if (this != null) LogUtil.w(tag, this) }

fun String?.logE(tag: String = TAG) = run { if (this != null) LogUtil.e(tag, this) }

fun String?.logWTF(tag: String = TAG) = run { if (this != null) LogUtil.wtf(tag, this) }
