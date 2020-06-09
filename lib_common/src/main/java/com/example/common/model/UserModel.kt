package com.example.common.model

class UserModel {
    var name: String? = null // 姓名
    var email: String? = null // 邮箱地址
    var sms_check: Boolean? = false // 短信验证是否开启(Y,N)
    var google_check: Boolean? = false // 谷歌验证是否开启
    var have_trade_pass: Boolean? = false // 是否设置了资金密码
    var card_no: String? = null // 证件号码
    var mobile: String? = null // 手机号码
    var user_id: String? = null // 用户id
    var area: String? = null // 地区
    var real_verified: Boolean? = false // 是否实名认证
    var customer_status: String? = null // 实名认证状态(init:初始化,inreview:提交认证中,verified:已认证,refused:认证失败)
}
