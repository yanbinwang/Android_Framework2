package com.example.thirdparty.pay.bean

import com.example.common.config.Constants
import com.google.gson.annotations.SerializedName
import com.tencent.mm.opensdk.modelpay.PayReq

data class PayBean(
    //用于微信
    @SerializedName("appid")
    var mAppId: String? = null,
    @SerializedName("partnerid")
    var mPartnerId: String? = null,
    @SerializedName("prepayid")
    var mPrepayId: String? = null,
    @SerializedName("noncestr")
    var mNonceStr: String? = null,
    @SerializedName("timestamp")
    var mTimestamp: String? = null,
    //支付宝，微信共用
    @SerializedName("sign")
    var mSign: String? = null
) {
    /**
     * 获取微信支付的对象
     */
    val wxPayReq get() = PayReq().apply {
        appId = Constants.WX_APP_ID
        partnerId = mPartnerId
        prepayId = mPrepayId
        packageValue = "Sign=WXPay"
        nonceStr = mNonceStr
        timeStamp = mTimestamp
        sign = mSign
    }
}