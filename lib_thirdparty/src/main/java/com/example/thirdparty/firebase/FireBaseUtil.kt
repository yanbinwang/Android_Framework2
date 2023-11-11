package com.example.thirdparty.firebase

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.common.BaseApplication
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.logE
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * 谷歌firebase工具栏
 * yan
 */
object FireBaseUtil {
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(BaseApplication.instance) }
    var tokenRefreshListener: ((String) -> Unit)? = null
    var notificationIntentGenerator = { _: Context, _: Map<String, String> -> Intent() }
    var notificationHandler: ((data: Map<String, String>) -> Boolean)? = null

    fun initSubApplication(application: Application) {
        FirebaseApp.initializeApp(application)
    }

    fun initTestReport() {
        if (isDebug) FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
    }

    fun refreshToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            tokenRefreshListener?.let { tokenRefreshListener ->
                val token = try {
                    it.result
                } catch (e: Exception) {
                    e.logE
                    null
                }
                if (!it.isSuccessful || token.isNullOrEmpty()) {
                    "Fetching FCM registration token failed".logE
                    return@addOnCompleteListener
                }
                tokenRefreshListener(token)
            }
        }
    }

    fun onDeepLink(activity: Activity, onSuccess: Uri.() -> Unit, onFail: () -> Unit) {
        Firebase.dynamicLinks
            .getDynamicLink(activity.intent)
            .addOnSuccessListener(activity) { pendingDynamicLinkData ->
                pendingDynamicLinkData?.link?.onSuccess() ?: onFail()
            }.addOnFailureListener(activity) { e ->
                e.logE
                onFail()
            }.addOnCanceledListener {
                onFail()
            }
    }

}