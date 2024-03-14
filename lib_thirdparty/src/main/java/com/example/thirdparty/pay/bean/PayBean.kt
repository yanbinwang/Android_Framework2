package com.example.thirdparty.pay.bean

data class PayBean(
    //用于微信
    var appid: String? = null,
    var partnerid: String? = null,
    var prepayid: String? = null,
    var noncestr: String? = null,
    var timestamp: String? = null,
    //支付宝，微信共用
    var sign: String? = null
)