package com.example.thirdparty.firebase

import com.example.framework.utils.function.value.isDebug
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * 日志收集
 * yan
 */
object CrashlyticsUtil {

    fun postError(msg: String, e: Exception) {
        if (!isDebug) FirebaseCrashlytics.getInstance().recordException(Exception(msg, e))
    }

}