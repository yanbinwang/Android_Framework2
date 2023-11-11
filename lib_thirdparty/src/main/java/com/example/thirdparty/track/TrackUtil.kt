package com.example.thirdparty.track

import com.example.common.BaseApplication
import com.example.common.utils.DeviceIdUtil
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toBundle
import com.example.thirdparty.appsFlyer.AppsFlyerUtil
import com.example.thirdparty.appsFlyer.AppsFlyerUtil.appsFlyerInstance
import com.example.thirdparty.firebase.FireBaseUtil.firebaseAnalytics

/**
 * yan
 */
object TrackUtil {
    internal var userId: String? = null
    internal var userSid: String? = null
    internal val deviceId: String? by lazy { DeviceIdUtil.deviceId }

    fun setUserId(userId: String?, userSid: String?) {
        this.userId = userId
        this.userSid = userSid

        firebaseAnalytics.setUserId(userSid ?: userId)
        appsFlyerInstance.setCustomerUserId(userSid ?: userId)
    }

    fun log(key: String, vararg pairs: Pair<String, Any?>) {
        if (isDebug) return

        val bundle = pairs.toBundle { this }
        userId?.let { bundle.putString("user_id", it) }
        userSid?.let { bundle.putString("user_sid", it) }
        deviceId?.let { bundle.putString("device_num", it) }

        firebaseAnalytics.logEvent(key, bundle)
//        facebookLogger.logEvent(key, bundle)
        AppsFlyerUtil.track(BaseApplication.instance, key, bundle)
    }

    fun TrackEvent.log(vararg pairs: Pair<String, Any?>) {
        log(tag, *pairs)
    }

    /**
     * banner点击数
     */
    fun homeBanner(banner: String) {
        TrackEvent.HOME_BANNER.log("banner" to banner)
    }

    /**
     * 头像（左上角）
     */
    fun homePortrait() {
        TrackEvent.HOME_PORTRAIT.log()
    }

    /**
     * 注册，获取验证码
     */
    fun smsCode() {
        TrackEvent.SMS_CODE.log()
    }

    /**
     * 注册，确认下一步
     */
    fun registerNext() {
        TrackEvent.REGISTER_NEXT.log()
    }

    /**
     * 所有注册成功
     */
    fun registerSuccess() {
        TrackEvent.REGISTER_SUCCESS.log()
    }

    /**
     * 邮箱登录成功
     */
    fun loginEmailSuccess() {
        TrackEvent.LOGIN_EMAIL_SUCCESS.log()
    }

    /**
     * 邮箱注册成功
     */
    fun registerEmailSuccess() {
        TrackEvent.REGISTER_MAIL_SUCCESS.log()
    }

    /**
     * 实名认证提交
     */
    fun certification() {
        TrackEvent.CERTIFICATION.log()
    }

    /**
     * 点击充值按钮次数
     * home->首页
     * market_sell->市场（出售）
     * balance->资金（点击充值按钮）
     * balance_detail资金（币种详情点击充值）
     */
    fun paymentClick(type: String) {
        TrackEvent.PAYMENT_CLICK.log("type" to type)
    }

}
