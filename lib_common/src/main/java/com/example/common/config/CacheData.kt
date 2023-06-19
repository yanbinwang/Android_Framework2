package com.example.common.config

import com.example.common.bean.UserBean
import com.example.common.utils.DataCacheUtil
import com.example.common.utils.DataStringCacheUtil

/**
 * @description 全局mmkv存储
 * @author yan
 */
object CacheData {
    //设备id
    private const val DEVICE_ID = "device_id"
    internal val deviceId = DataStringCacheUtil(DEVICE_ID)

    //用户类
    private const val USER_BEAN = "user_bean"
    internal val userBean = DataCacheUtil(USER_BEAN, UserBean::class.java)

}