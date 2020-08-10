package com.example.common.constant

import android.os.Environment

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
object Constants {
    //app内的一些默认值
    @JvmField
    var SCREEN_WIDTH = 0//手机宽度
    @JvmField
    var SCREEN_HEIGHT = 0//手机高度
    @JvmField
    var STATUS_BAR_HEIGHT = 0//导航栏高度
    @JvmField
    var IP: String? = null//当前手机ip
    @JvmField
    var MAC: String? = null//当前手机mac地址
    @JvmField
    var DEVICE_ID: String? = null//当前手机设备id
    @JvmField
    var APPLICATION_ID: String? = null//当前包名
    @JvmField
    var APPLICATION_NAME: String? = null//当前应用名
    @JvmField
    var APPLICATION_FILE_PATH: String? = null//默认文件保存路径，sd卡下的应用名文件夹
    @JvmField
    var SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath//sd卡的根路径mnt/sdcard

    //app内接口的一些默认配置字段
    const val LOGIN_INTERCEPTOR_CODE = 1 //阿里路由登录全局拦截器编号
    const val PUSH_NOTIFY_ID = 0 //固定通知id
    const val PUSH_CHANNEL_ID = "shuniuyun" //推送渠道id
    const val PUSH_CHANNEL_NAME = "数牛金服" //推送渠道名
    const val LIMIT = "10" //取的页数
    const val WX_APP_ID = "wx92fdc4b6ab9647cd" //微信的appId
    const val ADAPTER_ITEM_VIEW_TYPE_HEAD: Int = 1
    const val ADAPTER_ITEM_VIEW_TYPE_BODY = 2
    const val ADAPTER_ITEM_VIEW_TYPE_BOTTOM = 3

    //MMKV存储字段
    const val KEY_USER_MODEL = "keyUserModel" //用户类json

    //系统广播
    const val APP_USER_LOGIN = "com.bitnew.tech.APP_USER_LOGIN" //用户登录
    const val APP_USER_LOGIN_OUT = "com.bitnew.tech.APP_USER_LOGIN_OUT" //用户注销
    //    public static final String APP_USER_INFO_UPDATE = "com.bitnew.tech.APP_USER_INFO_UPDATE";//用户信息更新
    //    public static final String APP_SHARE_SUCCESS = "com.bitnew.tech.APP_SHARE_SUCCESS";//分享成功
    //    public static final String APP_SHARE_CANCEL = "com.bitnew.tech.APP_SHARE_CANCEL";//分享取消
    //    public static final String APP_SHARE_FAILURE = "com.bitnew.tech.APP_SHARE_FAILURE";//分享失败
}