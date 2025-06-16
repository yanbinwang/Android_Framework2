package com.example.thirdparty.track

import com.example.common.utils.DeviceIdUtil
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toBundle
import com.example.thirdparty.firebase.utils.FireBaseUtil.firebaseAnalytics
import com.example.thirdparty.track.bean.TrackEvent

/**
 * 埋点提交工具类
 */
object TrackUtil {
    private var userId: String? = null
    private val deviceId: String? by lazy { DeviceIdUtil.deviceId }

    /**
     * 用户登录调用该方法
     */
    @JvmStatic
    fun init(userId: String? = null) {
        this.userId = userId
        firebaseAnalytics.setUserId(userId)
    }

    @JvmStatic
    fun TrackEvent.log(vararg pairs: Pair<String, Any?>) {
        log(tag, *pairs)
    }

    @JvmStatic
    fun log(key: String, vararg pairs: Pair<String, Any?>) {
        if (isDebug || !isLogin()) return
        val bundle = pairs.toBundle { this }
        userId?.let { bundle.putString("user_id", it) }
        deviceId?.let { bundle.putString("device_num", it) }
        firebaseAnalytics.logEvent(key, bundle)
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

    /**
     * 注册，获取验证码
     */
    @JvmStatic
    fun smsCode() {
        TrackEvent.SMS_CODE.log()
    }

    /**
     * 注册，确认下一步
     */
    @JvmStatic
    fun registerNext() {
        TrackEvent.REGISTER_NEXT.log()
    }

    /**
     * 所有注册成功
     */
    @JvmStatic
    fun registerSuccess() {
        TrackEvent.REGISTER_SUCCESS.log()
    }

    /**
     * 邮箱注册成功
     */
    @JvmStatic
    fun registerEmailSuccess() {
        registerSuccess()
        TrackEvent.REGISTER_MAIL_SUCCESS.log()
    }

    /**
     * 邮箱登录成功
     */
    @JvmStatic
    fun loginEmailSuccess() {
        TrackEvent.LOGIN_EMAIL_SUCCESS.log()
    }

    /**
     * 实名认证提交
     */
    @JvmStatic
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
    @JvmStatic
    fun paymentClick(type: String) {
        TrackEvent.PAYMENT_CLICK.log("type" to type)
    }

}