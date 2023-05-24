package com.example.common.config

import com.example.common.bean.LanguageBean
import com.example.common.bean.UserBean
import com.example.common.utils.DataCacheUtil
import com.example.common.utils.DataStringCacheUtil
import com.example.common.utils.Language.Companion.en_IN

/**
 * @description 全局mmkv存储
 * @author yan
 */
object CacheData {
    //语言包
    private const val LANGUAGE_BEAN = "language_bean"
    val languageBean = DataCacheUtil(LANGUAGE_BEAN, LanguageBean::class.java)

    //語言(默認英語)
    private const val LANGUAGE = "language"
    val language = DataStringCacheUtil(LANGUAGE, en_IN)

    //设备id
    private const val DEVICE_ID = "device_id"
    val deviceId = DataStringCacheUtil(DEVICE_ID)

    //用户类
    private const val USER_BEAN = "user_bean"
    val userBean = DataCacheUtil(USER_BEAN, UserBean::class.java)
}