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
 * FireBaseUtil.initialize(applicationContext)
 * FireBaseUtil.notificationIntentGenerator = { _, map ->
 * " \n收到firebase\nmap:${map.toJson()}".logWTF
 * LinkActivity.byPush(instance, *map.toArray { it.key to it.value })
 * }
 * FireBaseUtil.tokenRefreshListener = {
 * "firebase token $it".logE
 * ConfigHelper.setDeviceToken(it)
 * }
 */
object FireBaseUtil {
    // 日志埋点
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(BaseApplication.instance.applicationContext) }
    // FirebaseService->onNewToken(获取到手机token)
    var tokenRefreshListener: ((String) -> Unit)? = null
    // FirebaseService->onMessageReceived(收到的推送消息体)
    var notificationHandler: ((data: Map<String, String>) -> Boolean)? = null
    // 构建推送的intent，掉起一个透明的页面LinkActivity然后处理跳转
    var notificationIntentGenerator = { _: Context, _: Map<String, String> -> Intent() }

    /**
     * firebase本身会自动注册，但会有延迟，此时显式调用
     */
    fun initialize(context: Context) {
        //初始化
        FirebaseApp.initializeApp(context)
        //收集错误日志（调试模式下不开启）
        if (isDebug) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
        //刷新手机token
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
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
            tokenRefreshListener?.invoke(token)
        }
    }

    /**
     * 深度推送（Deep Linking）
     * 指的是借助动态链接引导用户在应用内直接跳转到特定页面或执行特定操作，而不只是单纯打开应用
     * 生成动态链接：你可以借助 Firebase 控制台或者 Firebase SDK 来生成动态链接。在生成链接时，能够指定当用户点击链接后要跳转的应用内页面或者要执行的操作。
     * 用户点击链接：用户点击动态链接之后，系统会先判定该应用是否已经安装。
     * 应用已安装：应用会直接启动，并且依据链接中的参数跳转到指定的页面或者执行特定操作。
     * 应用未安装：用户会被引导至应用商店去下载安装应用，待安装完成并首次打开应用时，应用会依据链接参数跳转到指定页面或者执行特定操作。
     * 深度推送的应用场景
     * 分享内容：当用户在应用内分享文章、商品等内容时，能够生成包含深度链接的分享链接。其他用户点击这个链接，若已安装应用，就能直接查看分享的内容；若未安装应用，下载安装后也可直接查看。
     * 广告推广：在广告投放时运用深度链接，用户点击广告链接，若已安装应用，就会直接进入推广的页面；若未安装应用，下载安装后也能直接进入推广页面，增强用户体验和转化率。
     *
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
            FireBaseApi.instance.getBindFireBaseApi(reqBodyOf("token" to ConfigHelper.getDeviceToken())).apply { listener.invoke(successful()) }
        }
    }

}

private interface FireBaseApi {
    companion object {
        val instance by lazy { RetrofitFactory.instance.createByServer(FireBaseApi::class.java) }
    }

    /**
     * 获取绑定firebase接口
     */
    @POST("api/platformUser/bindFireBase")
    suspend fun getBindFireBaseApi(@Body body: RequestBody): ApiResponse<EmptyBean>
}