package com.example.thirdparty.firebase

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.ARouterUtil.jump
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.unicodeDecode

/**
 * 处理推送通知
 */
object JumpLinkUtil {
    private const val routerLink = "tradewills.com"//跳转地址
    private const val PUSH_URL = "pushUrl"//需要打开的url
    private const val PUSH_WEB_URL = "pushWebUrl"//需要打开的web的url
    private const val PUSH_IS_ADMIN = "pushIsAdmin"//推送来源：isAdmin不为空为后台推送，为空则是运营推送

    /**
     * SplashActivity页调取
     *  if (!isTaskRoot) {
     *     tryJump(this,
     *          onSuccess = { finish() },
     *          onFailed = { finish() })
     *    } else {
     *       initSplash()
     *      initSplashData()
     *   }
     */
    @JvmStatic
    fun tryJump(activity: FragmentActivity, onResult: (Boolean) -> Unit = {}) = activity.execute {
        //获取intent中是否包含对应跳转的值
        if (intent.extras?.size().orZero <= 0) {
            onResult(false)
            return@execute
        }
        //查找是否是firebase的深度推送
        val isDynamicLink = intent.extras?.keySet()?.find {
            it.contains("DYNAMIC_LINK", true) || it.contains("dynamiclink", true)
        } != null

        //先尝试走push
        if (handlePush(activity)) {
            onResult(true)
        } else if (isDynamicLink) {
            //push失败,尝试deeplink
            handleDeepLink(activity, onResult)
        } else {
            onResult(false)
        }
    }

    /**
     * 处理深度链接
     */
    @JvmStatic
    fun handleDeepLink(activity: FragmentActivity, onResult: (Boolean) -> Unit = {}) = activity.execute {
        FireBaseUtil.onDeepLink(this, {
            //deeplink成功
            try {
                jump(activity, this)
                onResult(true)
            } catch (e: Exception) {
                handleDeepLinkByFailed(activity, activity.intent.data, onResult)
            }
        }, { handleDeepLinkByFailed(activity, activity.intent.data, onResult) })
    }

    private fun handleDeepLinkByFailed(context: Context, uri: Uri?, onResult: (Boolean) -> Unit = {}) {
        if (uri?.host?.endsWith(routerLink).orFalse) {
            jump(context, uri)
            onResult(true)
        } else {
            onResult(false)
        }
    }

    /**
     * 处理推送
     */
    @JvmStatic
    fun handlePush(activity: FragmentActivity): Boolean {
        val url = if (activity.intentString(PUSH_IS_ADMIN).isEmpty()) {
            activity.intentString(PUSH_WEB_URL).unicodeDecode()
        } else {
            activity.intentString(PUSH_URL).unicodeDecode()
        }
        return if (url.isNullOrEmpty()) {
            false
        } else {
            jump(activity, url)
            true
        }
    }

}