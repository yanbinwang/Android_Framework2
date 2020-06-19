package com.example.framework.utils

import android.util.Log
import com.example.framework.BuildConfig

/**
 * 日志输出类
 */
object LogUtil {
    private const val debug = BuildConfig.ISDEBUG
    private const val defaultTag = "dota" // 默认的tag

    @JvmStatic
    fun v(msg: String) {
        if (debug) {
            Log.v(defaultTag, msg)
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (debug) {
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun d(msg: String) {
        if (debug) {
            Log.d(defaultTag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (debug) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun i(msg: String) {
        if (debug) {
            Log.i(defaultTag, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (debug) {
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    fun w(msg: String) {
        if (debug) {
            Log.w(defaultTag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        if (debug) {
            Log.w(tag, msg)
        }
    }

    @JvmStatic
    fun e(msg: String) {
        if (debug) {
            Log.e(defaultTag, msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (debug) {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun m() {
        if (debug) {
            val methodName = Exception().stackTrace[1].methodName
            Log.v(defaultTag, methodName)
        }
    }

    @JvmStatic
    fun m(msg: String) {
        if (debug) {
            val methodName = Exception().stackTrace[1].methodName
            Log.v(defaultTag, "$methodName:    $msg")
        }
    }
}
