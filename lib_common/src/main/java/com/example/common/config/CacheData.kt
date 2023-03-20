package com.example.common.config

import com.example.common.bean.UserBean
import com.example.common.utils.DataCacheUtil

/**
 * @description 全局mmkv存储
 * @author yan
 */
object CacheData {
    private const val USER_BEAN = "user_bean" //用户类
    val userBean = DataCacheUtil(USER_BEAN, UserBean::class.java)


}