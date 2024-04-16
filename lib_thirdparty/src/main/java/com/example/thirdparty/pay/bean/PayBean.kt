package com.example.thirdparty.pay.bean

import com.example.common.config.Constants
import com.tencent.mm.opensdk.modelpay.PayReq

data class PayBean(
    //用于微信
    var appid: String? = null,
    var partnerid: String? = null,
    var prepayid: String? = null,
    var noncestr: String? = null,
    var timestamp: String? = null,
    //支付宝，微信共用
    var sign: String? = null
) {
    /**
     * 获取微信支付的对象
     */
    val wxPayReq get() = PayReq().also {
        it.appId = Constants.WX_APP_ID
        it.partnerId = partnerid
        it.prepayId = prepayid
        it.packageValue = "Sign=WXPay"
        it.nonceStr = noncestr
        it.timeStamp = timestamp
        it.sign = sign
    }
}