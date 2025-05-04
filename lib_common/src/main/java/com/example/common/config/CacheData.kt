package com.example.common.config

import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.utils.DataBooleanCache
import com.example.common.utils.DataParcelableCache
import com.example.common.utils.DataStringCache

/**
 * @description 全局mmkv存储
 * @author yan
 */
object CacheData {
    //设备id
    private const val DEVICE_ID = "device_id"
    internal val deviceId = DataStringCache(DEVICE_ID)

    //是否同意告知书
    private const val PRIVACY_AGREED = "privacy_agreed"
    internal val privacyAgreed = DataBooleanCache(PRIVACY_AGREED)

    //用户类
    private const val USER_BEAN = "user_bean"
    internal val userBean = DataParcelableCache(USER_BEAN, UserBean::class.java)

    //用户信息类
    private val USER_INFO_BEAN = "user_info_bean"
    internal val userInfoBean = DataParcelableCache(USER_INFO_BEAN, UserInfoBean::class.java)

}