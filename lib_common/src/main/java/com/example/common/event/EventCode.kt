package com.example.common.event

import com.example.common.bean.UserInfoBean

object EventCode {
    //用户登录
    val EVENT_USER_LOGIN = Code<Any>()

    //用户注销
    val EVENT_USER_LOGIN_OUT = Code<Any>()

    //用户个人信息刷新
    val EVENT_USER_INFO_REFRESH = Code<UserInfoBean?>()

    //联网
    val EVENT_ONLINE = Code<Any>()

    //断网
    val EVENT_OFFLINE = Code<Any>()

    //按下Home键,菜单键,电源键
    val EVENT_MENU_ACTION = Code<Any>()

    //支付成功
    val EVENT_PAY_SUCCESS = Code<Any>()

    //支付取消
    val EVENT_PAY_CANCEL = Code<Any>()

    //支付失败
    val EVENT_PAY_FAILURE = Code<Any>()

}