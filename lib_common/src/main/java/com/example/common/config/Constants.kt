package com.example.common.config

import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.bean.ServerLanguage
import com.example.common.utils.DeviceIdUtil
import com.example.common.utils.helper.ConfigHelper.getAppName
import com.example.common.utils.helper.ConfigHelper.getAppVersionCode
import com.example.common.utils.helper.ConfigHelper.getAppVersionName
import com.example.common.utils.helper.ConfigHelper.getPackageName
import com.example.common.utils.i18n.string

/**
 * Created by wyb on 2017/3/7.
 * 配置文件，用于存放一些用得到的静态变量
 */
object Constants {
    //------app内的一些默认值------
    //版本号
    @JvmStatic
    val VERSION_CODE get() = getAppVersionCode()
    //版本名
    @JvmStatic
    val VERSION_NAME get() = getAppVersionName()
    //包名
    @JvmStatic
    val APPLICATION_ID get() = getPackageName()
    //应用名
    @JvmStatic
    val APPLICATION_NAME get() = getAppName()
    //默认文件保存路径，sd卡下的应用名文件夹
    @JvmStatic
    val APPLICATION_PATH get() = "${SDCARD_PATH}/${APPLICATION_NAME}"
    //sd卡的根路径/android/data/{包名}->访问这个目录不需要动态申请STORAGE权限
    @JvmStatic
    val SDCARD_PATH get() = BaseApplication.instance.getExternalFilesDir(null)?.absolutePath
    //设备id
    @JvmStatic
    val DEVICE_ID get() = DeviceIdUtil.deviceId
    //无数据占位符
    @JvmStatic
    val NO_DATA get() = string(R.string.unitNoData)
    //------app内接口的一些默认配置字段------
    //固定配置页数
    const val PAGE_LIMIT = "10"
    //语言包先写死
    val LANGUAGE_LIST by lazy { listOf(
        ServerLanguage(0, "zh_HK", "繁體中文", "", "1"),
        ServerLanguage(1, "en_IN", "English", "", "1"),
        ServerLanguage(2, "id_ID", "Bahasa Indonesia", "", "1")) }
    //------socket地址------
    //訂單
    const val SOCKET_DEAL_URL = "/user/topic/console/subscribe/pendingOrder"
    //廣告
    const val SOCKET_ADVERTISE_URL = "/user/topic/console/subscribe/entrustInProgress"
    //資金
    const val SOCKET_FUNDS_URL = "/user/topic/console/subscribe/assetInfo"
}