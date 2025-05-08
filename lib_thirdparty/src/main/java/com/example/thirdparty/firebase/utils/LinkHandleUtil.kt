package com.example.thirdparty.firebase.utils

import androidx.activity.ComponentActivity
import com.example.common.utils.manager.ARouterManager
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.unicodeDecode

/**
 * 处理推送通知
 */
object LinkHandleUtil {
//    private const val routerLink = "tradewills.com"//跳转地址

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
//     *  onResult-》是否处理，false的话启动页自己处理页面跳转
//     */
//    @JvmStatic
//    fun tryJump(activity: ComponentActivity, resp: (Boolean) -> Unit = {}) = activity.execute {
//        //获取intent中是否包含对应跳转的值
//        if (intent.extras?.size().orZero <= 0) {
//            resp.invoke(false)
//            return@execute
//        }
////        //查找是否是firebase的深度推送
////        val isDynamicLink = intent.extras?.keySet()?.find {
////            it.contains("DYNAMIC_LINK", true) || it.contains("dynamiclink", true)
////        } != null
//        //先尝试走push
//        if (handlePush(activity)) {
//            resp.invoke(true)
////        } else if (isDynamicLink) {
////            //push失败,尝试deeplink
////            handleDeepLink(activity, resp)
//        } else {
//            resp.invoke(false)
//        }
//    }
//
//    /**
//     * 处理普通推送
//     */
//    @JvmStatic
//    fun handlePush(activity: ComponentActivity): Boolean {
//        //推送来源
//        val linkType = activity.intentString("linkType").unicodeDecode()//推送类别
//        val linkInfo = activity.intentString("linkInfo").unicodeDecode()//推送类型
//        val businessId = activity.intentString("businessId").unicodeDecode()//ID
//        val skipWay = linkType.toSafeInt()
//        return if (linkType.isNullOrEmpty() || linkInfo.isNullOrEmpty() || (skipWay != 1 && skipWay != 2)) {
//            false
//        } else {
//            ARouterManager().jump(skipWay = skipWay, url = linkInfo, id = businessId)
//            true
//        }
//    }
//
//    /**
//     * 处理深度链接
//     */
//    @JvmStatic
//    fun handleDeepLink(activity: ComponentActivity, resp: (Boolean) -> Unit = {}) = activity.execute {
//        FireBaseUtil.onDeepLink(this, {
//            try {
//                // 解析深度链接中的参数
//                val param = getQueryParameter("param")
//                if (param != null) {
//                    val intent = Intent(this, DetailActivity::class.java)
//                    intent.putExtra("param", param)
//                    startActivity(intent)
//                }
//                jump(activity, this)
//                resp.invoke(true)
//            } catch (e: Exception) {
//                handleDeepLinkByFailed(activity, activity.intent.data, resp)
//            }
//        }, {
//            handleDeepLinkByFailed(activity, activity.intent.data, resp)
//        })
//    }
//
//    private fun handleDeepLinkByFailed(context: Context, uri: Uri?, resp: (Boolean) -> Unit = {}) {
//        if (uri?.host?.endsWith(routerLink).orFalse) {
//            jump(context, uri)
//            resp.invoke(true)
//        } else {
//            resp.invoke(false)
//        }
//    }

}