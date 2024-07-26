package com.example.common.utils.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bean.UserBean
import com.example.common.bean.UserInfoBean
import com.example.common.config.ARouterPath
import com.example.common.config.CacheData.userBean
import com.example.common.config.CacheData.userInfoBean
import com.example.common.event.EventCode.EVENT_USER_INFO_REFRESH
import com.example.common.utils.manager.AppManager
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
    private fun setUser(bean: UserBean?) {
        bean ?: return
        userBean.set(bean)
    }

    /**
     * 获取用户对象
     */
    fun getUser(): UserBean {
        return userBean.get() ?: UserBean()
    }

    /**
     * 获取userid
     */
    fun getUserId(): String {
        return getUser().userId.orEmpty()
    }

    /**
     * 获取token
     */
    fun getToken(): String {
        return getUser().token.orEmpty()
    }

    /**
     * 是否通过实名认证
     */
    fun getIsReal(): Boolean {
        return getUser().isReal.orFalse
    }

    /**
     * 存储手机号
     */
    fun setPhoneNumber(newPhoneNumber: String?) {
        newPhoneNumber ?: return
        getUser().let {
            it.phoneNumber = newPhoneNumber
            setUser(it)
        }
    }

    /**
     * 获取手机号
     */
    fun getPhoneNumber(): String {
        return getUser().phoneNumber.orEmpty()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户基本信息">
    /**
     * 存储用户信息对象
     */
    private fun setUserInfo(bean: UserInfoBean?) {
        bean ?: return
        if (getUserInfo() == bean) return//重写equals和hashcode
        userInfoBean.set(bean)
    }

    /**
     * 获取用户信息对象
     */
    fun getUserInfo(): UserInfoBean {
        return userInfoBean.get() ?: UserInfoBean()
    }

    /**
     * 设置账户状态
     * 0冻结 1正常
     */
    fun setStatus(newStatus: Int?) {
        newStatus ?: return
        getUserInfo().let {
            it.status = newStatus
            setUserInfo(it)
        }
    }

    /**
     * 获取余额->balance+sendBalance
     */
    fun getLumpSum(): String {
        return getUserInfo().let {
            it.balance.add(it.sendBalance.orEmpty())
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="通用用户工具类方法">
    /**
     * 刷新个人信息
     */
    fun refresh(bean: UserInfoBean?) {
        bean ?: return
        if (getUserInfo() == bean) return
        setUserInfo(bean)
        EVENT_USER_INFO_REFRESH.post(userInfoBean.get())
    }

    /**
     * 是否登陆
     */
    fun isLogin(): Boolean {
        return getUser().let {
            !it.token.isNullOrEmpty()
        }
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
     * MainActivity中注册EVENT_USER_LOGIN_OUT广播，关闭除其外的所有activity
     * 如果需要跳转别的页面再调取ARouter，默认会拉起登录
     */
    fun signOut(isNavigation: Boolean = true) {
        userBean.del()
        userInfoBean.del()
//        WebSocketConnect.disconnect()
//        EVENT_USER_LOGIN_OUT.post()
        AppManager.finishAll()
//        ARouter.getInstance().build(ARouterPath.StartActivity).navigation()
        if (isNavigation) {
            ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
        }
    }
    // </editor-fold>

}