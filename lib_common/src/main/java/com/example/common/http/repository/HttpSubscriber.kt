package com.example.common.http.repository

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.constant.ARouterPath
import com.example.common.utils.helper.AccountHelper

/**
 * Created by WangYanBin on 2020/9/3.
 * 继承原先的监听后对结果做处理（用于项目中的接口请求，可不去实现成功和失败）
 * onSuccess->成功直接将外层剥离取得对象T
 * onFailed->失败返回失败异常，或服务器给予的失败提示文案
 */
abstract class HttpSubscriber<T> : ResourceSubscriber<ApiResponse<T>>() {

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    override fun onResult(data: ApiResponse<T>?, throwable: Throwable?) {
        if (null != data) {
            val msg = data.msg
            val e = data.e
            if (0 == e) {
                onSuccess(data.data)
            } else {
                //账号还没有登录，解密失败，重新获取
                if (100005 == e || 100008 == e) {
                    AccountHelper.signOut()
//                         instance.post(RxBusEvent(Constants.APP_USER_LOGIN_OUT))
                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
                }
                //账号被锁定--进入账号锁定页（其余页面不关闭）
                if (100002 == e) {
//                         ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation()
                }
                onFailed(throwable, msg)
            }
        } else {
            onFailed(throwable, "")
        }
    }
    // </editor-fold>

    /**
     * 请求成功，直接回调对象
     */
    open fun onSuccess(data: T?) {}

    /**
     * 请求失败，获取失败原因
     */
    open fun onFailed(e: Throwable?, msg: String?) {}

}