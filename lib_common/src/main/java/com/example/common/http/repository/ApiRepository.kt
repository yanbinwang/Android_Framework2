package com.example.common.http.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败)
 */
object ApiRepository {

    suspend fun <T> call(request: T?, resourceSubscriber: ResourceSubscriber<T>?) {
        resourceSubscriber?.onStart()
        try {
            val res: T? = withContext(IO) { request }
            res?.let {
                resourceSubscriber?.onNext(it)
            }
        } catch (e: Exception) {
            resourceSubscriber?.onError(e)
        } finally {
            resourceSubscriber?.onComplete()
        }
    }

    suspend fun <T> apiCall(request: ApiResponse<T>?, subscriber: HttpSubscriber<T>?) {
        subscriber?.onStart()
        try {
            val res: ApiResponse<T>? = withContext(IO) { request }
            res?.let {
                subscriber?.onNext(it)
            }
        } catch (e: Exception) {
            subscriber?.onError(e)
        } finally {
            subscriber?.onComplete()
        }
    }

}