package com.example.thirdparty.track

import com.example.common.utils.DeviceIdUtil
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toBundle
import com.example.thirdparty.firebase.utils.FireBaseUtil.firebaseAnalytics
import com.example.thirdparty.firebase.utils.FireBaseUtil.firebaseCrashlytics
import com.example.thirdparty.track.bean.TrackEvent

/**
 * 埋点/异常提交提交工具类
 */
object TrackUtil {
    private var mUserId: String? = null

    /**
     * 用户登录调用该方法
     */
    fun initialize(userId: String? = null) {
        mUserId = userId
        firebaseAnalytics?.setUserId(userId)
    }

    /**
     * 上报闪退/异常日志
     */
    fun String.record(e: Throwable) {
        recordException(this, e)
    }

    fun recordException(msg: String, e: Throwable) {
        if (isDebug) return
        firebaseCrashlytics.recordException(Exception(msg, e))
    }

    /**
     * 上报统计/埋点日志
     */
    fun TrackEvent.log(vararg pairs: Pair<String, Any?>) {
        logEvent(tag, *pairs)
    }

    fun logEvent(key: String, vararg pairs: Pair<String, Any?>) {
        if (isDebug || !isLogin()) return
        val bundle = pairs.toBundle { this }
        mUserId?.let { bundle.putString("user_id", it) }
        DeviceIdUtil.deviceId?.let { bundle.putString("device_num", it) }
        firebaseAnalytics?.logEvent(key, bundle)
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
     * 邮箱注册成功
     */
    fun registerEmailSuccess() {
        registerSuccess()
        TrackEvent.REGISTER_MAIL_SUCCESS.log()
    }

    /**
     * 邮箱登录成功
     */
    fun loginEmailSuccess() {
        TrackEvent.LOGIN_EMAIL_SUCCESS.log()
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