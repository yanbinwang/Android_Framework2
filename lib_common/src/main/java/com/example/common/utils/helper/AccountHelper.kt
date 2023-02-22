package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.config.Constants
import com.example.common.utils.AppManager
import com.example.common.utils.DataCacheUtil

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 */
object AccountHelper {
    private val MMKV_USER_BEAN = "${Constants.APPLICATION_ID}.UserBean" //用户类
    private val userBean = DataCacheUtil(MMKV_USER_BEAN, UserBean::class.java)

    //存储用户对象
    fun setUserBean(bean: UserBean?) {
        bean ?: return
        userBean.set(bean)
    }

    //获取用户对象
    fun getUserBean(): UserBean? {
        return userBean.get()
    }

    //用户是否登陆
    fun isLogin(): Boolean {
        val bean = getUserBean()
        bean ?: return false
        return !bean.token.isNullOrEmpty()
    }

    //获取手机号
    fun getMobile(): String? {
        val bean = getUserBean()
        bean ?: return null
        return bean.mobile
    }

    fun setMobile(mobile: String?) {
        val bean = getUserBean()
        bean?.mobile = mobile
        setUserBean(bean)
    }

    //用户注销操作（清除信息,清除用户凭证）
    fun signOut() {
        userBean.del()
//        MmkvUtil.apply {
//            removeValueForKey(MMKV_USER_BEAN)
////            removeValueForKey(MMKV_USER_INFO_BEAN)
//        }
        AppManager.finishAll()
        ARouter.getInstance().build(ARouterPath.StartActivity).navigation()
    }

}