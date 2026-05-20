package com.example.framework.utils

import android.util.Log
import com.example.framework.utils.function.value.isDebug

/**
 * 日志输出类
 */
private const val TAG = "dota" // 默认的tag
private const val MAX_LOG_LENGTH = 4000

object LogUtil {
    fun v(tag: String = TAG, msg: String) = logIfDebug(tag, Log.VERBOSE, msg)
    fun d(tag: String = TAG, msg: String) = logIfDebug(tag, Log.DEBUG, msg)
    fun i(tag: String = TAG, msg: String) = logIfDebug(tag, Log.INFO, msg)
    fun w(tag: String = TAG, msg: String) = logIfDebug(tag, Log.WARN, msg)
    fun e(tag: String = TAG, msg: String) = logIfDebug(tag, Log.ERROR, msg)
    fun wtf(msg: String) = logIfDebug(TAG, Log.ASSERT, msg)
    fun wtf(tag: String = TAG, msg: String) = logIfDebug(tag, Log.ASSERT, msg)

    private fun logIfDebug(tag: String, level: Int, message: String) {
        if (isDebug) {
            if (message.length <= MAX_LOG_LENGTH) {
                Log.println(level, tag, message)
            } else {
                for (i in message.indices step MAX_LOG_LENGTH) {
                    val end = (i + MAX_LOG_LENGTH).coerceAtMost(message.length)
                    Log.println(level, tag, message.substring(i, end))
                }
            }
        }
    }
}

val Throwable?.logE: Unit get() { this?.toString()?.logE }
val Throwable?.logWTF: Unit get() { this?.toString()?.logWTF }

val String?.logV: Unit get() { this?.let { LogUtil.v(msg = it) } }
val String?.logD: Unit get() { this?.let { LogUtil.d(msg = it) } }
val String?.logI: Unit get() { this?.let { LogUtil.i(msg = it) } }
val String?.logW: Unit get() { this?.let { LogUtil.w(msg = it) } }
val String?.logE: Unit get() { this?.let { LogUtil.e(msg = it) } }
val String?.logWTF: Unit get() { this?.let { LogUtil.wtf(msg = it) } }

fun String?.logV(tag: String = TAG) = this?.let { LogUtil.v(tag, it) }
fun String?.logD(tag: String = TAG) = this?.let { LogUtil.d(tag, it) }
fun String?.logI(tag: String = TAG) = this?.let { LogUtil.i(tag, it) }
fun String?.logW(tag: String = TAG) = this?.let { LogUtil.w(tag, it) }
fun String?.logE(tag: String = TAG) = this?.let { LogUtil.e(tag, it) }
fun String?.logWTF(tag: String = TAG) = this?.let { LogUtil.wtf(tag, it) }