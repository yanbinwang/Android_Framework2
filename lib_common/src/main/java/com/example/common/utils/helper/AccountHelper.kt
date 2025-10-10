package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userBean
import com.example.common.config.CacheData.userInfoBean
import com.example.common.event.EventCode.EVENT_USER_INFO_REFRESH
import com.example.common.event.EventCode.EVENT_USER_LOGIN_OUT
import com.example.common.utils.manager.AppManager
import com.example.common.utils.manager.CacheDataManager
import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.orFalse

/**
 * Created by WangYanBin on 2020/8/11.
 * 用户信息做了规整和管控，全局直接调用
 * 注意get值一定要有，否则xml中取值会报错
 */
object AccountHelper {

    // <editor-fold defaultstate="collapsed" desc="用户类方法">
    /**
     * 存储用户对象
     */
    @JvmStatic
    private fun setUser(bean: UserBean?) {
        bean ?: return
        userBean.set(bean)
    }

    /**
     * 获取用户对象
     */
    @JvmStatic
    fun getUser(): UserBean {
        return userBean.get() ?: UserBean()
    }

    /**
     * 获取userid
     */
    @JvmStatic
    fun getUserId(): String {
        return getUser().userId.orEmpty()
    }

    /**
     * 获取token
     */
    @JvmStatic
    fun getToken(): String {
        return getUser().token.orEmpty()
    }

    /**
     * 是否通过实名认证
     */
    @JvmStatic
    fun getIsReal(): Boolean {
        return getUser().isReal.orFalse
    }

    /**
     * 存储手机号
     */
    @JvmStatic
    fun setPhoneNumber(phoneNumber: String) {
        val bean = getUser()
        bean.phoneNumber = phoneNumber
        setUser(bean)
    }

    /**
     * 获取手机号
     */
    @JvmStatic
    fun getPhoneNumber(): String {
        return getUser().phoneNumber.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户基本信息">
    /**
     * 存储用户信息对象
     */
    @JvmStatic
    private fun setUserInfo(bean: UserInfoBean?) {
        bean ?: return
        if (getUserInfo() == bean) return//重写equals和hashcode
        userInfoBean.set(bean)
    }

    /**
     * 获取用户信息对象
     */
    @JvmStatic
    fun getUserInfo(): UserInfoBean {
        return userInfoBean.get() ?: UserInfoBean()
    }

    /**
     * 设置账户状态
     * 0冻结 1正常
     */
    @JvmStatic
    fun setStatus(status: Int) {
        val bean = getUserInfo()
        bean.status = status
        setUserInfo(bean)
    }

    /**
     * 获取余额->balance+sendBalance
     */
    @JvmStatic
    fun getLumpSum(): String {
        return getUserInfo().let {
            it.balance.add(it.sendBalance)
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="通用用户工具类方法">
    /**
     * 刷新个人信息
     */
    @JvmStatic
    fun refresh(bean: UserInfoBean?, isPost: Boolean = true) {
        bean ?: return
        if (getUserInfo() == bean) return
        setUserInfo(bean)
        if(isPost) EVENT_USER_INFO_REFRESH.post(userInfoBean.get())
    }

    /**
     * 是否登陆
     */
    @JvmStatic
    fun isLogin(): Boolean {
        return getUser().let {
            !it.token.isNullOrEmpty()
        }
    }

    /**
     * 登录成功调取（初始化一些登录后才进行的操作，第三方库初始化）
     */
    @JvmStatic
    fun signIn(bean: UserBean?) {
        bean ?: return
        setUser(bean)
    }

    /**
     * 用户注销操作（清除信息,清除用户凭证，第三方库注销）
     * MainActivity中注册EVENT_USER_LOGIN_OUT广播，关闭除其外的所有activity
     * 如果需要跳转别的页面再调取ARouter，默认会拉起登录
     */
    @JvmStatic
    fun signOut(isNavigation: Boolean = true) {
        // 1.清除mmkv和默认配置的数据库等缓存数据
        userBean.del()
        userInfoBean.del()
        CacheDataManager.clearCacheBySignOut()
        // 2.断开/终止三方库的连接(其内部应包含数据的删除)
//        WebSocketConnect.disconnect()
        // 3.根据app的实际情况分为一下两种处理
        /**
         * App需要强制登录后才能进入首页
         * 1)isNavigation: Boolean = true删除
         * 2)拉起透明页面,通过AppManager.reboot
         * 3)LoginActivity/StartActivity使用singleTask
         */
        AppManager.reboot(ARouterPath.LoginActivity)

        /**
         * App无需强制登录就能进入,但是会在首页或者初次启动/引导的页面打开登录
         * 1)isNavigation: Boolean = true保留,部分页面无需强制拉起首页
         * 2)LoginActivity使用singleTop
         */
        EVENT_USER_LOGIN_OUT.post()
        if (isNavigation) {
            ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
        }
    }
    // </editor-fold>

}