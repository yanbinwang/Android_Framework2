package com.example.common.bus

import com.example.common.constant.Constants.APPLICATION_ID

/**
 * @description
 * @author
 */
object EventCode {
    //用户登录
    const val EVENT_USER_LOGIN = "${APPLICATION_ID}.EVENT_USER_LOGIN"
    //用户注销
    const val EVENT_USER_LOGIN_OUT = "${APPLICATION_ID}.EVENT_USER_LOGIN_OUT"
}