package com.dataqin.common.constant

import com.dataqin.common.BaseApplication

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
    //------gradle4.1.0开始，库中不提供VERSION_CODE和VERSION_NAME，在主app中获取，赋值给全局静态变量------
    @JvmField
    var VERSION_CODE: Long = 0//版本号
    @JvmField
    var VERSION_NAME: String? = null//版本名
    //------------
    @JvmField
    var IP: String? = null//当前手机ip
    @JvmField
    var MAC: String? = null//当前手机mac地址
    @JvmField
    var DEVICE_ID: String? = null//当前手机设备id
    @JvmField
    var APPLICATION_FILE_PATH: String? = null//默认文件保存路径，sd卡下的应用名文件夹
    @JvmField
    var SDCARD_PATH = BaseApplication.instance?.getExternalFilesDir(null)?.absolutePath//sd卡的根路径mnt/sdcard-访问这个目录不需要动态申请STORAGE权限
//    var SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath//sd卡的根路径mnt/sdcard

    //app内接口的一些默认配置字段
    const val APPLICATION_ID = "com.sqkj.sxevidence"//当前包名
    const val APPLICATION_NAME = "尚存"//当前应用名
    const val LOGIN_INTERCEPTOR_CODE = 1 //阿里路由登录全局拦截器编号
    const val PUSH_NOTIFY_ID = 0 //固定通知id
    const val PUSH_CHANNEL_ID = "dataqin" //推送渠道id
    const val PUSH_CHANNEL_NAME = "数秦科技" //推送渠道名
//    const val WX_APP_ID = "wx92fdc4b6ab9647cd" //微信的appId
    const val CAMERA_FILE_PATH = "Photo"
    const val VIDEO_FILE_PATH = "Video"
    const val AUDIO_FILE_PATH = "Audio"
    const val SCREEN_FILE_PATH = "Screen"

    //MMKV存储字段
    const val KEY_USER_MODEL = "keyUserModel" //用户类json
    const val KEY_USER_INFO_MODEL = "keyUserInfoModel" //用户信息类json

    //系统广播
    const val LIVE_DATA_KEY = "com.sqkj.oea.LIVE_DATA_KEY"//总消息订阅key
    const val APP_USER_LOGIN = "com.sqkj.sxevidence.APP_USER_LOGIN"//用户登录
    const val APP_USER_LOGIN_OUT = "com.sqkj.sxevidence.APP_USER_LOGIN_OUT"//用户注销
    const val APP_USER_INFO_UPDATE = "com.sqkj.sxevidence.APP_USER_INFO_UPDATE"//用户注销
    const val APP_USER_POINTS_UPDATE = "com.sqkj.oes.APP_USER_POINTS_UPDATE"//用户积分更新
    const val APP_USER_RECOGNITION_QUERY = "com.sqkj.sxevidence.APP_USER_RECOGNITION_QUERY"//用户人脸识别认证查询
    const val APP_EVIDENCE_SELECT = "com.sqkj.sxevidence.APP_EVIDENCE_SELECT"//选中证据库
    const val APP_EVIDENCE_SELECT_ALL = "com.sqkj.sxevidence.APP_EVIDENCE_SELECT_ALL"//选中证据库文件待上传
    const val APP_EVIDENCE_UPDATE = "com.sqkj.sxevidence.APP_EVIDENCE_UPDATE"//更新数据,每一个证据文件上传成功后
    const val APP_EVIDENCE_EXTRAS_UPDATE = "com.sqkj.sxevidence.APP_EVIDENCE_EXTRAS_UPDATE"//更新文件待上传数据
    const val APP_ACTION_SHUTDOWN = "com.sqkj.sxevidence.APP_ACTION_SHUTDOWN"//应用销毁
    const val APP_ACTION_CLOSE_SYSTEM_DIALOGS = "com.sqkj.sxevidence.APP_ACTION_CLOSE_SYSTEM_DIALOGS"//按下home或menu键
    const val APP_SCREEN_FILE_CREATE = "com.sqkj.sxevidence.APP_SCREEN_FILE_CREATE"//录频文件创建
    const val APP_PAY_SUCCESS = "com.sqkj.sxevidence.APP_PAY_SUCCESS"//支付成功
    const val APP_PAY_CANCEL = "com.sqkj.sxevidence.APP_PAY_CANCEL"//支付取消
    const val APP_PAY_FAILURE = "com.sqkj.sxevidence.APP_PAY_FAILURE"//支付失败
//    const val APP_SHARE_SUCCESS = "com.sqkj.sxevidence.APP_SHARE_SUCCESS"//分享成功
//    const val APP_SHARE_CANCEL = "com.sqkj.sxevidence.APP_SHARE_CANCEL"//分享取消
//    const val APP_SHARE_FAILURE = "com.sqkj.sxevidence.APP_SHARE_FAILURE"//分享失败
}