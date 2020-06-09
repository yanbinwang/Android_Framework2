package com.example.framework.utils

import android.util.Log
import com.example.framework.BuildConfig

/**
 * 日志输出类
 */
object LogUtil {
    private const val debug = BuildConfig.ISDEBUG
    private const val defaultTag = "dota" // 默认的tag

    fun v(msg: String) {
        if (debug) {
            Log.v(defaultTag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (debug) {
            Log.v(tag, msg)
        }
    }

    fun d(msg: String) {
        if (debug) {
            Log.d(defaultTag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (debug) {
            Log.d(tag, msg)
        }
    }

    fun i(msg: String) {
        if (debug) {
            Log.i(defaultTag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (debug) {
            Log.i(tag, msg)
        }
    }

    fun w(msg: String) {
        if (debug) {
            Log.w(defaultTag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (debug) {
            Log.w(tag, msg)
        }
    }

    fun e(msg: String) {
        if (debug) {
            Log.e(defaultTag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (debug) {
            Log.e(tag, msg)
        }
    }

    fun m() {
        if (debug) {
            val methodName = Exception().stackTrace[1].methodName
            Log.v(defaultTag, methodName)
        }
    }

    fun m(msg: String) {
        if (debug) {
            val methodName = Exception().stackTrace[1].methodName
            Log.v(defaultTag, "$methodName:    $msg")
        }
    }
}
