package com.example.common.http

import com.example.common.http.callback.ApiResponse
import com.example.common.http.callback.HttpObserver
import com.example.common.http.callback.HttpSubscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/8/27.
 * 协程工具类，用于框架内网络请求，本身即协程体
 */
class HttpCoroutine : CoroutineScope {
    private var completableJob = SupervisorJob()//此ViewModel运行的所有协程所用的任务,终止这个任务将终止此ViewModel开始的所有协程
    private var coroutineScope = CoroutineScope(Dispatchers.Main + completableJob)//所有协程的主作用域

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

    fun cancel() {
        completableJob.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = coroutineScope.launch { }

}