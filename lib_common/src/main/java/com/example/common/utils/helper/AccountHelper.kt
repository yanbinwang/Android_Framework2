package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserAuthBean
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userAuthBean
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
    private fun setUser(bean: UserBean?) {
        bean ?: return
        userBean.set(bean)
    }

    /**
     * 获取用户对象
     */
    private fun getUser(): UserBean? {
        return userBean.get()
    }

    /**
     * 获取userid
     */
    fun getUserId(): String {
        return getUser()?.userId.orEmpty()
    }

    /**
     * 获取token
     */
    fun getToken(): String {
        return getUser()?.token.orEmpty()
    }

    /**
     * 是否通过实名认证
     */
    fun getIsReal(): Boolean {
        return getUser()?.isReal.orFalse
    }

    /**
     * 存储手机号
     */
    fun setPhoneNumber(phoneNumber: String?) {
        val bean = getUser()
        bean?.phoneNumber = phoneNumber
        setUser(bean)
    }

    /**
     * 获取手机号
     */
    fun getPhoneNumber(): String {
        return getUser()?.phoneNumber.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户基本信息">
    /**
     * 存储用户信息对象
     */
    private fun setUserInfo(bean: UserInfoBean?) {
        bean ?: return
        userInfoBean.set(bean)
    }

    /**
     * 获取用户信息对象
     */
    private fun getUserInfo(): UserInfoBean? {
        return userInfoBean.get()
    }

    /**
     * 设置账户状态
     * 0冻结 1正常
     */
    fun setStatus(status: Int?) {
        val bean = getUserInfo()
        bean?.status = status
        setUserInfo(bean)
    }

    /**
     * 获取余额->balance+sendBalance
     */
    fun getLumpSum(): String {
        return getUserInfo()?.let {
            it.balance.add(it.sendBalance.orEmpty())
        }.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户认证信息">
    /**
     * 存储用户认证状态类
     */
    private fun setUserAuth(bean: UserAuthBean?) {
        bean ?: return
        userAuthBean.set(bean)
    }

    /**
     * 获取用户对象（一定会有值）
     */
    private fun getUserAuth(): UserAuthBean {
        return userAuthBean.get()
    }

    /**
     * 通过认证直接调用
     * 设置当前身份的审核状态
     * 1：待审核 2：审核被拒 3：审核通过
     */
    fun setRealStatus(realStatus: Int?) {
        val bean = getUserAuth()
        bean.realStatus = realStatus
        setUserAuth(bean)
    }

    /**
     * 设置当前身份
     * 0:个人认证，1:企业认证
     */
    fun setUserType(userType: String?) {
        val bean = getUserAuth()
        bean.userType = userType
        setUserAuth(bean)
    }

    /**
     * 是否是kol用户
     */
    fun getKolStatus(): Boolean {
        return getUserAuth().kolStatus == 2
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="通用用户工具类方法">
    /**
     * 个人信息更新
     */
    fun update(bean: UserInfoBean?) {
        bean ?: return
        setUserInfo(bean)
    }

    /**
     * 个人认证信息更新
     */
    fun update(bean: UserAuthBean?) {
        bean ?: return
        setUserAuth(bean)
    }

    /**
     * 刷新个人认证信息
     */
    fun refresh(bean: UserAuthBean?) {
        bean ?: return
        update(bean)
//        EVENT_USER_DATA_REFRESH.post(userData.get())
    }

    /**
     * 是否登陆
     */
    fun isLogin(): Boolean {
        val bean = getUser()
        bean ?: return false
        return !bean.token.isNullOrEmpty()
    }

    /**
     * 登录成功调取（初始化一些登录后才进行的操作，第三方库初始化）
     */
    fun signIn(bean: UserBean?) {
        bean ?: return
        setUser(bean)
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