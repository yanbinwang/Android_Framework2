package com.example.thirdparty.firebase.utils

import com.example.framework.utils.function.value.isDebug
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * 日志上传
 */
object CrashlyticsUtil {

    fun recordException(msg: String, e: Exception) {
        if (!isDebug) FirebaseCrashlytics.getInstance().recordException(Exception(msg, e))
    }

}