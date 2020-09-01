package com.example.common.http.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/1.
 */
open class HttpRepository {

    suspend fun <T : Any> apiCall(call: suspend () -> ApiResponse<T>): ApiResponse<T> {
        return withContext(Dispatchers.IO) { call.invoke() }.apply {
            //特殊编码处理
            when (e) {
                //账号还没有登录，解密失败，重新获取
                100005, 100008 -> {
//                    AccountHelper.signOut();
//                    RxBus.getInstance().post(new RxBusEvent(Constants.APP_USER_LOGIN_OUT));
//                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation();
                }
                //账号被锁定--进入账号锁定页（其余页面不关闭）
//                if (100002 == e) {
//                    ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation();
//                }
            }
        }
    }

}