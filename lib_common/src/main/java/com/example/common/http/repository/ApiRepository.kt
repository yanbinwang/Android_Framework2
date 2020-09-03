package com.example.common.http.repository

import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.constant.ARouterPath
import com.example.common.utils.helper.AccountHelper


/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败，成功返回对象，失败会上抛异常)
 */
object ApiRepository {

    fun <T> apiCall(call: ApiResponse<T>, subscriber: HttpSubscriber<T>) {
        try {
            val msg = call.msg
            val e = call.e
            if (0 == e) {
                subscriber.onSuccess(call.data)
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
                subscriber.onFailed(null, msg)
            }
        } catch (e: Exception) {
            subscriber.onFailed(e, "")
        }
    }

}