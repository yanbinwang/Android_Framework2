package com.example.thirdparty.pay.bean

import com.example.common.config.Constants
import com.tencent.mm.opensdk.modelpay.PayReq

data class PayBean(
    //支付宝支付
    var orderInfo: String? = null,
    var orderNo: String? = null,
    //微信支付
    var appId: String? = null,
    var nonceStr: String? = null,
    var packageValue: String? = null,
    var partnerId: String? = null,
    var prepayId: String? = null,
    var sign: String? = null,
    var timeStamp: String? = null
) {
    /**
     * 获取微信支付的对象
     */
    val wxPayReq get() = PayReq().also {
        it.appId = Constants.WX_APP_ID
        it.partnerId = partnerId
        it.prepayId = prepayId
        it.packageValue = "Sign=WXPay"
        it.nonceStr = nonceStr
        it.timeStamp = timeStamp
        it.sign = sign
    }
}