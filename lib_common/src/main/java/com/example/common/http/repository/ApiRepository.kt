package com.example.common.http.repository

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败，成功返回对象，失败会上抛异常)
 */
object ApiRepository {

    suspend fun <T : Any> apiCall(call: suspend () -> ApiResponse<T>, subscriber: HttpSubscriber<T>?) {
        subscriber?.onStart()
        try {
            var t: ApiResponse<T>? = null
            withContext(IO) { t = call.invoke() }
            subscriber?.onNext(t)
        } catch (e: Exception) {
            subscriber?.onFailed(e, "")
        } finally {
            subscriber?.onComplete()
        }
    }

}