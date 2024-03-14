package com.example.common.event

import com.example.common.bean.UserInfoBean

object EventCode {
    //語言切換
    val EVENT_LANGUAGE_CHANGE = Code<Any>()

    //联网
    val EVENT_ONLINE = Code<Any>()

    //断网
    val EVENT_OFFLINE = Code<Any>()

    //用户登录
    val EVENT_USER_LOGIN = Code<Any>()

    //用户注销
    val EVENT_USER_LOGIN_OUT = Code<Any>()

    //用户个人信息刷新
    val EVENT_USER_INFO_REFRESH = Code<UserInfoBean?>()

}