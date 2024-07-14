package com.example.thirdparty.firebase.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.common.BaseApplication
import com.example.common.network.factory.RetrofitFactory
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.common.network.repository.reqBodyOf
import com.example.common.network.repository.successful
import com.example.common.utils.helper.ConfigHelper
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.logE
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * firebase推送
 */
object FireBaseUtil {
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(BaseApplication.instance) }
    var tokenRefreshListener: ((String) -> Unit)? = null
    var notificationIntentGenerator = { _: Context, _: Map<String, String> -> Intent() }
    var notificationHandler: ((data: Map<String, String>) -> Boolean)? = null

    fun initSubApplication(application: Application) {
        FirebaseApp.initializeApp(application)
    }

    /**
     *  调试模式下不收集错误日志
     */
    fun initTestReport() {
        if (isDebug) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

    /**
     * 刷新手机token
     */
    fun refreshToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            tokenRefreshListener?.let { listen ->
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
                listen(token)
            }
        }
    }

    /**
     * 获取深度连接
     */
    fun onDeepLink(activity: Activity, onSuccess: Uri.() -> Unit, onFailed: () -> Unit) {
        Firebase.dynamicLinks
            .getDynamicLink(activity.intent)
            .addOnSuccessListener(activity) { dynamicLinkData ->
                dynamicLinkData?.link?.onSuccess() ?: onFailed()
            }.addOnFailureListener(activity) { e ->
                e.logE
                onFailed()
            }.addOnCanceledListener {
                onFailed()
            }
    }

    /**
     * 绑定
     */
    suspend fun bind(isBind: Boolean, listener: (isBind: Boolean) -> Unit = {}) {
        if (!isBind) {
            FireBaseApi.fireBaseApi.getBindFireBaseApi(reqBodyOf("token" to ConfigHelper.getDeviceToken())).apply { listener.invoke(successful()) }
        }
    }

}

private interface FireBaseApi {
    companion object {
        val fireBaseApi get() = RetrofitFactory.instance.createByServer(FireBaseApi::class.java)
    }

    /**
     * 获取绑定firebase接口
     */
    @POST("api/platformUser/bindFireBase")
    suspend fun getBindFireBaseApi(@Body body: RequestBody): ApiResponse<EmptyBean>
}