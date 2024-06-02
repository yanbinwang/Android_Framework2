package com.example.thirdparty.firebase

/**
 * 处理推送通知
 */
object LinkHandleUtil {
////    private const val routerLink = "tradewills.com"//跳转地址
//    private val builder by lazy { ARouterBuilder() }
//
//    /**
//     * SplashActivity页调取
//     *  if (!isTaskRoot) {
//     *     tryJump(this,
//     *          onSuccess = { finish() },
//     *          onFailed = { finish() })
//     *    } else {
//     *       initSplash()
//     *      initSplashData()
//     *   }
//     */
//    @JvmStatic
//    fun tryJump(activity: AppCompatActivity, onResult: (Boolean) -> Unit = {}) = activity.execute {
//        //获取intent中是否包含对应跳转的值
//        if (intent.extras?.size().orZero <= 0) {
//            onResult.invoke(false)
//            return@execute
//        }
//        //查找是否是firebase的深度推送
//        val isDynamicLink = intent.extras?.keySet()?.find {
//            it.contains("DYNAMIC_LINK", true) || it.contains("dynamiclink", true)
//        } != null
//        //先尝试走push
//        if (handlePush(activity)) {
//            onResult.invoke(true)
//        } else if (isDynamicLink) {
//            //push失败,尝试deeplink
////            handleDeepLink(activity, onResult)
//            onResult.invoke(false)
//        } else {
//            onResult.invoke(false)
//        }
//    }
//
////    /**
////     * 处理深度链接
////     */
////    @JvmStatic
////    fun handleDeepLink(activity: AppCompatActivity, onResult: (Boolean) -> Unit = {}) = activity.execute {
////        FireBaseUtil.onDeepLink(this, {
////            //deeplink成功
////            try {
////                jump(activity, this)
////                onResult(true)
////            } catch (e: Exception) {
////                handleDeepLinkByFailed(activity, activity.intent.data, onResult)
////            }
////        }, { handleDeepLinkByFailed(activity, activity.intent.data, onResult) })
////    }
////
////    private fun handleDeepLinkByFailed(context: Context, uri: Uri?, onResult: (Boolean) -> Unit = {}) {
////        if (uri?.host?.endsWith(routerLink).orFalse) {
////            jump(context, uri)
////            onResult.invoke(true)
////        } else {
////            onResult.invoke(false)
////        }
////    }
//
//    /**
//     * 处理推送
//     */
//    @JvmStatic
//    fun handlePush(activity: AppCompatActivity): Boolean {
//        //推送来源
//        val linkType = activity.intentString("linkType").unicodeDecode()//推送类别
//        val linkInfo = activity.intentString("linkInfo").unicodeDecode()//推送类型
//        val businessId = activity.intentString("businessId").unicodeDecode()//ID
//        val skipWay = linkType.toSafeInt()
//        return if (linkType.isNullOrEmpty() || linkInfo.isNullOrEmpty() || (skipWay != 1 && skipWay != 2)) {
//            false
//        } else {
//            builder.jump(skipWay = skipWay, url = linkInfo, id = businessId)
//            true
//        }
//    }

}