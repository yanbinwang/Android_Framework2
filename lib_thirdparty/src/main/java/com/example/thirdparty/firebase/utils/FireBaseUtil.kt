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
        /**
         * 功能：启用 Crashlytics 崩溃报告功能。
         * 作用：
         * 自动收集应用运行时的崩溃信息（如 NullPointerException、ANR 等），并上传到 Firebase 控制台。
         * 提供详细的堆栈跟踪、设备信息和用户行为日志，帮助开发者快速定位问题。
         * 注意：
         * 需要在 Firebase 控制台配置应用并下载google-services.json文件。
         * 可通过setUserId()关联用户身份，通过log()添加自定义日志。
         * 在调试阶段可暂时禁用：setCrashlyticsCollectionEnabled(BuildConfig.DEBUG)。
         */
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!isDebug)
        /**
         * 功能：启用 Firebase Analytics 用户行为分析。
         * 作用：
         * 自动跟踪用户留存、活跃度、转化漏斗等关键指标。
         * 支持自定义事件（如点击按钮、完成注册）和用户属性（如会员等级）。
         * 在 Firebase 控制台生成可视化报表，辅助产品决策。
         * 注意：
         * 数据默认会进行聚合和匿名化处理，符合隐私规范。
         * 可通过logEvent()记录自定义事件，通过setUserProperty()设置用户属性。
         * 国内环境可能需要配置网络代理才能正常上报数据。
         */
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!isDebug)
//        /**
//         * 功能：订阅 FCM（Firebase Cloud Messaging）的主题推送。
//         * 作用：
//         * 将设备注册到名为default的主题，服务器可向该主题广播消息，所有订阅设备都会收到。
//         * 适用于新闻推送、活动通知等一对多场景。
//         * 注意：
//         * 主题名称可自定义（如promotions、news），支持动态订阅 / 取消。
//         * 需要在服务器端使用 FCM API 发送主题消息（如通过 Firebase 控制台或后端 SDK）。
//         * 消息到达后，需在FirebaseMessagingService的子类中处理（如显示通知）。
//         *  限制类型	                       具体要求	                                                 示例说明
//         * 字符集限制	    仅支持字母（a-z、A-Z）、数字（0-9）、下划线（_）、连字符（-）、点（.）	     合法：news_sports、tech-2025
//         * 禁止特殊字符	不支持中文、空格、斜杠（/）、@、# 等特殊字符	                         非法：新闻/体育、包名.测试
//         * 长度限制	    主题名长度需控制在 256 个字符以内（实际业务中建议不超过 50 字符便于维护）	 过长名称可能导致解析异常
//         * 层级结构限制	主题名可使用点（.）模拟层级（如 tech.android），但本质是平面结构而非目录	 与 /topics/tech/android 效果相同
//         */
//        FirebaseMessaging.getInstance().subscribeToTopic("default")
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