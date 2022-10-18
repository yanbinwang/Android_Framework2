package com.example.common.bus

import com.example.common.constant.Constants.APPLICATION_ID

/**
 * @description
 * @author
 */
object EventCode {
    //系统广播
    const val LIVE_DATA_BUS_KEY = "${APPLICATION_ID}.LIVE_DATA_BUS_KEY"//总消息订阅key
    const val APP_USER_LOGIN = "${APPLICATION_ID}.APP_USER_LOGIN" //用户登录
    const val APP_USER_LOGIN_OUT = "${APPLICATION_ID}.APP_USER_LOGIN_OUT" //用户注销
    //    public static final String APP_USER_INFO_UPDATE = "com.bitnew.tech.APP_USER_INFO_UPDATE";//用户信息更新
    //    public static final String APP_SHARE_SUCCESS = "com.bitnew.tech.APP_SHARE_SUCCESS";//分享成功
    //    public static final String APP_SHARE_CANCEL = "com.bitnew.tech.APP_SHARE_CANCEL";//分享取消
    //    public static final String APP_SHARE_FAILURE = "com.bitnew.tech.APP_SHARE_FAILURE";//分享失败
}