package com.example.common.config

import com.example.common.BaseApplication

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
object Constants {
    //app内的一些默认值
    //------gradle4.1.0开始，库中不提供VERSION_CODE和VERSION_NAME，在主app中获取，赋值给全局静态变量------
    @JvmField
    var VERSION_CODE: Long = 0//版本号
    @JvmField
    var VERSION_NAME: String? = null//版本名
    //------------
//    @JvmField
//    var IP: String? = null//当前手机ip
//    @JvmField
//    var MAC: String? = null//当前手机mac地址
//    @JvmField
//    var DEVICE_ID: String? = null//当前手机设备id
    @JvmField
    var APPLICATION_FILE_PATH: String? = null//默认文件保存路径，sd卡下的应用名文件夹
    @JvmField
    var SDCARD_PATH = BaseApplication.instance.getExternalFilesDir(null)?.absolutePath//sd卡的根路径mnt/sdcard-访问这个目录不需要动态申请STORAGE权限

    //app内接口的一些默认配置字段
    const val APPLICATION_ID = "com.sqkj.oea"//当前包名
    const val APPLICATION_NAME = "简证"//当前应用名
    const val LOGIN_INTERCEPTOR_CODE = 1 //阿里路由登录全局拦截器编号
    const val PUSH_NOTIFY_ID = 0 //固定通知id
    const val PUSH_CHANNEL_ID = "dataqin" //推送渠道id
    const val PUSH_CHANNEL_NAME = "数秦科技" //推送渠道名

}