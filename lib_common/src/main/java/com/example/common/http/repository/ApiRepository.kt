package com.example.common.http.repository

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败，成功返回对象，失败会上抛异常)
 */
open class ApiRepository {

    fun apiMessage(e: Exception): String? {
        return when (e) {
            is TokenInvalidException -> {
//                AccountHelper.signOut()
//                instance.post(RxBusEvent(Constants.APP_USER_LOGIN_OUT))
//                ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
                e.message
            }
            is IpLockedException -> {
//                ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation()
                e.message
            }
            is ServersException -> {
                e.message
            }
            else -> {
                ""
            }
        }
    }

    suspend fun <T : Any> apiCall(call: suspend () -> ApiResponse<T>): ApiResponse<T> {
        return withContext(IO) { call.invoke() }.apply {
            //请求编号特殊处理
            if (e != 200) {
                when (e) {
                    //账号被锁定--进入账号锁定页（其余页面不关闭）
                    100002 -> throw TokenInvalidException(msg)
                    //账号还没有登录，解密失败，重新获取
                    100005, 100008 -> throw IpLockedException(msg)
                    //其余非200情况都抛出异常
                    else -> ServersException(msg)
                }
            }
        }
    }

    class TokenInvalidException(msg: String? = null) : Exception(msg)

    class IpLockedException(msg: String? = null) : Exception(msg)

    class ServersException(msg: String? = null) : Exception(msg)

}