package com.example.common.config

import com.example.common.BaseApplication
import com.example.common.utils.helper.ConfigHelper

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
object Constants {
    //app内的一些默认值
    //------gradle4.1.0开始，库中不提供VERSION_CODE和VERSION_NAME，在主app中获取，赋值给全局静态变量------
    //版本号
    val VERSION_CODE: Long get() { return ConfigHelper.getAppVersionCode() }
    //版本名
    val VERSION_NAME: String get() { return ConfigHelper.getAppVersionName() }
    //------------
    //默认文件保存路径，sd卡下的应用名文件夹
    val APPLICATION_FILE_PATH: String get() { return "${SDCARD_PATH}/${APPLICATION_NAME}" }
    //sd卡的根路径mnt/sdcard-访问这个目录不需要动态申请STORAGE权限
    val SDCARD_PATH by lazy { BaseApplication.instance.getExternalFilesDir(null)?.absolutePath }
    //app内接口的一些默认配置字段
    const val APPLICATION_ID = "com.sqkj.oea"//当前包名
    const val APPLICATION_NAME = "简证"//当前应用名
    const val LOGIN_INTERCEPTOR_CODE = 1 //阿里路由登录全局拦截器编号
    const val PUSH_NOTIFY_ID = 0 //固定通知id
    const val PUSH_CHANNEL_ID = "dataqin" //推送渠道id
    const val PUSH_CHANNEL_NAME = "数秦科技" //推送渠道名

}