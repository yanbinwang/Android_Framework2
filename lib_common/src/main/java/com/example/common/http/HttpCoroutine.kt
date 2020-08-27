package com.example.common.http

import com.example.common.http.callback.ApiResponse
import com.example.common.http.callback.HttpObserver
import com.example.common.http.callback.HttpSubscriber
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Created by WangYanBin on 2020/8/27.
 * 用于框架内网络请求
 */
object HttpCoroutine {

    fun <T> observe(t: ApiResponse<T>, subscriber: HttpSubscriber<T>?) {
        observe(t, object : HttpObserver<ApiResponse<T>> {

            override fun onStart() {
                subscriber?.onStart()
            }

            override fun onNext(t: ApiResponse<T>?) {
                subscriber?.onNext(t)
            }

            override fun onComplete() {
                subscriber?.onComplete()
            }

        })
    }

    fun <T> observe(t: T, subscriber: HttpObserver<T>?) {
        subscriber?.onStart()
        subscriber?.onNext(t)
        subscriber?.onComplete()
    }

}