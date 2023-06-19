package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userBean
import com.example.common.config.CacheData.userInfoBean
import com.example.common.config.Constants
import com.example.common.utils.AppManager
import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.orFalse

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 * 注意get值一定要有，否则xml中取值会报错
 */
object AccountHelper {
    //默认用户文件保存位置
    val storage get() = "${Constants.APPLICATION_PATH}/手机文件/${getUserId()}/"

    // <editor-fold defaultstate="collapsed" desc="用户类方法">
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
     * 获取userid
     */
    fun getUserId(): String {
        return getUserBean()?.userId.orEmpty()
    }

    /**
     * 获取token
     */
    fun getToken(): String {
        return getUserBean()?.token.orEmpty()
    }

    /**
     * 是否通过实名认证
     */
    fun getIsReal(): Boolean {
        return getUserBean()?.isReal.orFalse
    }

    /**
     * 存储手机号
     */
    fun setPhoneNumber(phoneNumber: String?) {
        val bean = getUserBean()
        bean?.phoneNumber = phoneNumber
        setUserBean(bean)
    }

    /**
     * 获取手机号
     */
    fun getPhoneNumber(): String {
        return getUserBean()?.phoneNumber.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户基本信息">
    /**
     * 存储用户信息对象
     */
    private fun setUserInfoBean(bean: UserInfoBean?) {
        bean ?: return
        userInfoBean.set(bean)
    }

    /**
     * 获取用户信息对象
     */
    private fun getUserInfoBean(): UserInfoBean? {
        return userInfoBean.get()
    }

    /**
     * 设置账户状态
     * 0冻结 1正常
     */
    fun setStatus(status: Int?) {
        val bean = getUserInfoBean()
        bean?.status = status
        setUserInfoBean(bean)
    }

    /**
     * 获取余额->balance+sendBalance
     */
    fun getLumpSum(): String {
        return getUserInfoBean()?.let {
            it.balance.add(it.sendBalance.orEmpty())
        }.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="通用用户工具类方法">
    /**
     * 个人中心刷新时获取用户信息后调取当前方法
     */
    fun refreshUser(newData: UserBean?) {
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
    // </editor-fold>

}