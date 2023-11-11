package com.example.home.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.unicodeDecode
import com.example.thirdparty.firebase.FireBaseUtil

/**
 * 处理推送
 */
object LinkRouterUtil {

    fun handleDeepLink(act: Activity, onSuccess: () -> Unit, onFailed: () -> Unit) {
        FireBaseUtil.onDeepLink(act, {
            //deeplink成功
            try {
//                jump(act, this)
                onSuccess()
            } catch (e: Exception) {
//                handleRouterLink(act, act.intent.data, onSuccess, onFailed)
                onFailed()
            }
        }, {
//            handleRouterLink(act, act.intent.data, onSuccess, onFailed)
            onFailed()
        })
    }

    /**
     * @return 是否含有跳转链接
     */
    fun handlePush(act: Activity): Boolean {
//        // 推送来源：isAdmin不为空为后台推送，为空则是运营推送
//        val url = if (act.intentString(NotificationHelper.PUSH_IS_ADMIN).isEmpty()) {
//            // 点击通知栏跳转
//            act.intentString(NotificationHelper.WEB_URL).unicodeDecode()
//        } else {
//            act.intentString("url").unicodeDecode()
//        }
        // 推送来源：isAdmin不为空为后台推送，为空则是运营推送
        val url = act.intentString("url").unicodeDecode()
        return if (url.isNullOrEmpty()) {
            false
        } else {
//            jump(act, url)
            true
        }
    }

    fun tryJump(act: Activity, onSuccess: () -> Unit, onFailed: () -> Unit) {
        if (act.intent.extras?.size().orZero <= 0) {
            onFailed()
            return
        }

        val isDynamicLink = act.intent.extras?.keySet()?.find {
            it.contains("DYNAMIC_LINK", true) || it.contains("dynamiclink", true)
        } != null
        //先尝试走push
        if (handlePush(act)) {
            onSuccess()
        } else if (isDynamicLink) {
            //push失败,尝试deeplink
            handleDeepLink(act, onSuccess, onFailed)
        } else {
            onFailed()
        }
    }

//    fun handleRouterLink(context: Context, uri: Uri?, onSuccess: () -> Unit, onFailed: () -> Unit) {
//        if (uri?.host?.endsWith(routerLink).orFalse) {
//            jump(context, uri)
//            onSuccess()
//        } else {
//            onFailed()
//        }
//    }

}