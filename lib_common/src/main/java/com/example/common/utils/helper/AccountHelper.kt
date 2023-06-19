package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userBean
import com.example.common.config.Constants
import com.example.common.utils.AppManager

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 */
object AccountHelper {
    //默认用户文件保存位置
    val storage get() = "${Constants.APPLICATION_PATH}/手机文件/${getUserId()}/"

    /**
     * 存储用户对象
     * bean：修改后的值
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
     * 存储手机号
     */
    fun setMobile(mobile: String?) {
        val bean = getUserBean()
        bean?.mobile = mobile
        setUserBean(bean)
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
     * 获取userid
     */
    fun getUserId(): String? {
        val bean = getUserBean()
        bean ?: return null
        return bean.user_id
    }

    /**
     * 获取token
     */
    fun getToken(): String? {
        val bean = getUserBean()
        bean ?: return null
        return bean.token
    }

    /**
     * 个人中心刷新时获取用户信息后调取当前方法
     */
    fun refresh(newData: UserBean?) {
        newData ?: return
        setUserBean(newData)
//        EVENT_USER_DATA_REFRESH.post(userDataCache)
    }

    /**
     * 是否登陆
     */
    fun isLogin(): Boolean {
        val bean = getUserBean()
        bean ?: return false
        return !bean.token.isNullOrEmpty()
    }

    /**
     * 登录成功调取（初始化一些登录后才进行的操作，第三方库初始化）
     */
    fun signIn(bean: UserBean?) {
        bean ?: return
        setUserBean(bean)
    }

    /**
     * 用户注销操作（清除信息,清除用户凭证，第三方库注销）
     */
    fun signOut() {
        userBean.del()
        AppManager.finishAll()
        ARouter.getInstance().build(ARouterPath.StartActivity).navigation()
    }

}