package com.example.common.http.repository

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败，成功返回对象，失败会上抛异常)
 */
open class ApiRepository {

    suspend fun <T : Any> apiDispose(call: suspend () -> ApiResponse<T>): ApiResponse<T> {
        return withContext(IO) { call.invoke() }.apply {
            if (200 != e) {
                //特殊编号处理
                when (e) {
                    100005, 100008 -> throw TokenInvalidException()
                    100002 -> throw IpLockedException()
                    else -> throw ServerException(msg)
                }
            }
        }
    }

    class IpLockedException(msg: String? = null) : Exception(msg)

    class TokenInvalidException(msg: String? = null) : Exception(msg)

    class ServerException(msg: String? = null) : Exception(msg)

    companion object {

        fun <T> apiCall(call: ApiResponse<T>, subscriber: HttpSubscriber<T>) {
            subscriber.onStart()
            try {
                val t = call
                subscriber.onNext(t)
            } catch (e: Exception) {
                subscriber.onNext(null)
            }
            subscriber.onComplete()
        }

    }

}