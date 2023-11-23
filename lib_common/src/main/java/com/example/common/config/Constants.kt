package com.example.common.config

import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.DeviceIdUtil
import com.example.common.utils.function.string
import com.example.common.utils.helper.ConfigHelper.getAppName
import com.example.common.utils.helper.ConfigHelper.getAppVersionCode
import com.example.common.utils.helper.ConfigHelper.getAppVersionName
import com.example.common.utils.helper.ConfigHelper.getPackageName

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
object Constants {
    //------app内的一些默认值------
    //版本号
    val VERSION_CODE get() = getAppVersionCode()
    //版本名
    val VERSION_NAME get() = getAppVersionName()
    //包名
    val APPLICATION_ID get() = getPackageName()
    //应用名
    val APPLICATION_NAME get() = getAppName()
    //默认文件保存路径，sd卡下的应用名文件夹
    val APPLICATION_PATH get() = "${SDCARD_PATH}/${APPLICATION_NAME}"
    //sd卡的根路径/android/data/{包名}->访问这个目录不需要动态申请STORAGE权限
    val SDCARD_PATH get() = BaseApplication.instance.getExternalFilesDir(null)?.absolutePath
    //设备id
    val DEVICE_ID get() = DeviceIdUtil.deviceId
    //无数据占位符
    val NO_DATA get() = string(R.string.unit_no_data)
//    //无数据占位符
//    val NO_DATA_DOLLAR get() = string(R.string.unit_no_data_dollar)
//    //无数据占位符
//    val NO_DATA_PERCENT get() = string(R.string.unit_no_data_percent)
    //------app内接口的一些默认配置字段------
    const val PAGE_LIMIT = "10"//固定配置页数
}