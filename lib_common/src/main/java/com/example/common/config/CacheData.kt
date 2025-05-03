package com.example.common.config

import com.example.common.bean.LanguageBean
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.utils.DataBooleanCacheUtil
import com.example.common.utils.DataCacheUtil
import com.example.common.utils.DataStringCacheUtil

/**
 * @description 全局mmkv存储
 * @author yan
 */
object CacheData {
    //设备id
    private const val DEVICE_ID = "device_id"
    internal val deviceId = DataCacheUtil<String>(DEVICE_ID)

    //推送token
    private const val DEVICE_TOKEN = "device_token"
    internal val deviceToken = DataStringCacheUtil(DEVICE_TOKEN)

    //当前选择语言
    private const val LANGUAGE = "language"
    internal val language = DataStringCacheUtil(LANGUAGE)

    //语言类
    private const val LANGUAGE_BEAN = "language_bean"
    internal val languageBean = DataCacheUtil(LANGUAGE_BEAN, LanguageBean::class.java)

    //是否同意告知书
    private const val PRIVACY_AGREED = "privacy_agreed"
    internal val privacyAgreed = DataBooleanCacheUtil(PRIVACY_AGREED)

    //用户类
    private const val USER_BEAN = "user_bean"
    internal val userBean = DataCacheUtil(USER_BEAN, UserBean::class.java)

    //用户信息类
    private const val USER_INFO_BEAN = "user_info_bean"
    internal val userInfoBean = DataCacheUtil(USER_INFO_BEAN, UserInfoBean::class.java)

}