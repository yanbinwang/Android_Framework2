package com.example.common.config

import com.example.common.bean.LanguageBean
import com.example.common.bean.UserAuthBean
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
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

    //语言
    private const val LANGUAGE = "language"
    internal val language = DataStringCacheUtil(LANGUAGE)

    //语言對象
    private const val LANGUAGE_BEAN = "language_bean"
    internal val languageBean = DataCacheUtil(LANGUAGE_BEAN, LanguageBean::class.java)

    //用户类
    private const val USER_BEAN = "user_bean"
    internal val userBean = DataCacheUtil(USER_BEAN, UserBean::class.java)

    //用户信息类
    private val USER_INFO_BEAN = "user_info_bean"
    internal val userInfoBean = DataCacheUtil(USER_INFO_BEAN, UserInfoBean::class.java)

    //用户认证状态类
    private val USER_AUTH_BEAN = "user_auth_bean"
    internal val userAuthBean = DataCacheUtil(USER_AUTH_BEAN, UserAuthBean::class.java)

}