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
    fun v(msg: String) {
        if (debug) Log.v(TAG, msg)
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (debug) Log.v(tag, msg)
    }

    @JvmStatic
    fun d(msg: String) {
        if (debug) Log.d(TAG, msg)
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (debug) Log.d(tag, msg)
    }

    @JvmStatic
    fun i(msg: String) {
        if (debug) Log.i(TAG, msg)
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (debug) Log.i(tag, msg)
    }

    @JvmStatic
    fun w(msg: String) {
        if (debug) Log.w(TAG, msg)
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        if (debug) Log.w(tag, msg)
    }

    @JvmStatic
    fun e(msg: String) {
        if (debug) Log.e(TAG, msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (debug) Log.e(tag, msg)
    }

    @JvmStatic
    fun m() {
        if (debug) Log.v(TAG, Exception().stackTrace[1].methodName)
    }

    @JvmStatic
    fun m(msg: String) {
        if (debug) Log.v(TAG, "${Exception().stackTrace[1].methodName}:    $msg")
    }

}