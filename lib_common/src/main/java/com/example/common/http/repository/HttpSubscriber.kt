package com.example.common.http.repository

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.constant.ARouterPath
import com.example.common.utils.helper.AccountHelper

/**
 * Created by WangYanBin on 2020/9/3.
 */
interface HttpSubscriber<T> : ResourceSubscriber<ApiResponse<T>> {

    override fun onStart() {
    }

    override fun onNext(t: ApiResponse<T>?) {
        if (null != t) {
            val msg = t.msg
            val e = t.e
            if (0 == e) {
                onSuccess(t.data)
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
                onFailed(null, msg)
            }
        } else {
            onFailed(null, "")
        }
    }

    override fun onComplete() {
    }


    /**
     * 请求成功，直接回调对象
     */
    fun onSuccess(data: T?)

    /**
     * 请求失败，获取失败原因
     */
    fun onFailed(e: Throwable?, msg: String?)

}