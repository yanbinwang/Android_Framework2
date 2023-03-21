package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userBean
import com.example.common.utils.AppManager

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 */
object AccountHelper {

    /**
     * 存储用户对象
     */
    private fun setUserBean(bean: UserBean?) {
        bean ?: return
        userBean.set(bean)
    }

    /**
     * 获取用户对象
     */
    private fun getUserBean(): UserBean? {
        return userBean.get()
    }

    /**
     * 获取手机号
     */
    fun getMobile(): String? {
        val bean = getUserBean()
        bean ?: return null
        return bean.mobile
    }

    /**
     * 存储手机号
     */
    fun setMobile(mobile: String?) {
        val bean = getUserBean()
        bean?.mobile = mobile
        setUserBean(bean)
    }

    /**
     * 用户是否登陆
     */
    fun isLogin(): Boolean {
        val bean = getUserBean()
        bean ?: return false
        return !bean.token.isNullOrEmpty()
    }

    /**
     * 登录成功调取
     */
    fun signIn(bean: UserBean?) {
        setUserBean(bean)
    }

    /**
     * 用户注销操作（清除信息,清除用户凭证）
     */
    fun signOut() {
        userBean.del()
        AppManager.finishAll()
        ARouter.getInstance().build(ARouterPath.StartActivity).navigation()
    }

}