package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.config.Constants
import com.example.common.utils.AppManager
import com.example.common.utils.MmkvUtil

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 */
object AccountHelper {
    private const val MMKV_USER_BEAN = "${Constants.APPLICATION_ID}.UserBean" //用户类

    //存储用户对象
    @JvmStatic
    fun setUserBean(bean: UserBean?) {
        bean ?: return
        MmkvUtil.encode(MMKV_USER_BEAN, bean)
    }

    //获取用户对象
    @JvmStatic
    fun getUserBean(): UserBean? {
        return MmkvUtil.decodeParcelable(MMKV_USER_BEAN, UserBean::class.java)
    }

    //用户是否登陆
    @JvmStatic
    fun isLogin(): Boolean {
        val bean = getUserBean()
        bean ?: return false
        return !bean.token.isNullOrEmpty()
    }

    //获取手机号
    @JvmStatic
    fun getMobile(): String? {
        val bean = getUserBean()
        bean ?: return null
        return bean.mobile
    }

    @JvmStatic
    fun setMobile(mobile: String?) {
        val bean = getUserBean()
        bean?.mobile = mobile
        setUserBean(bean)
    }

    //用户注销操作（清除信息,清除用户凭证）
    @JvmStatic
    fun signOut() {
        MmkvUtil.apply {
            removeValueForKey(MMKV_USER_BEAN)
//            removeValueForKey(MMKV_USER_INFO_BEAN)
        }
        AppManager.finishAll()
        ARouter.getInstance().build(ARouterPath.StartActivity).navigation()
    }

}