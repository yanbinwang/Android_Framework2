package com.example.thirdparty.track

import com.example.common.utils.DeviceIdUtil
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toBundle
import com.example.thirdparty.facebook.FacebookAuthUtil.Companion.facebookLogger
import com.example.thirdparty.firebase.FireBaseUtil.firebaseAnalytics

/**
 * 埋点提交工具类
 */
object TrackUtil {
    private var userId: String? = null
    //    private var userSid: String? = null
    private val deviceId: String? by lazy { DeviceIdUtil.deviceId }

    /**
     * 用户登录调用该方法
     */
    fun init(userId: String? = null, userSid: String? = null) {
        this.userId = userId
//        this.userSid = userSid
        firebaseAnalytics.setUserId(userSid ?: userId)
    }

    @JvmStatic
    fun TrackEvent.log(vararg pairs: Pair<String, Any?>) {
        log(tag, *pairs)
    }

    @JvmStatic
    fun log(key: String, vararg pairs: Pair<String, Any?>) {
        if (isDebug) return
        if (!isLogin()) {
            init()
            return
        }
        val bundle = pairs.toBundle { this }
        userId?.let { bundle.putString("user_id", it) }
//        userSid?.let { bundle.putString("user_sid", it) }
        deviceId?.let { bundle.putString("device_num", it) }
        firebaseAnalytics.logEvent(key, bundle)
        facebookLogger.logEvent(key, bundle)
    }

    /**
     * banner点击数
     */
    @JvmStatic
    fun homeBanner(banner: String) {
        TrackEvent.HOME_BANNER.log("banner" to banner)
    }

    /**
     * 头像（左上角）
     */
    @JvmStatic
    fun homePortrait() {
        TrackEvent.HOME_PORTRAIT.log()
    }

}