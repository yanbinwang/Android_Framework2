package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/8/27.
 * 用于框架内网络请求
 */
object HttpCall {

    fun <T> apiCall(call: ApiResponse<T>, subscriber: HttpSubscriber<T>?) {
        subscriber?.onStart()
        subscriber?.onNext(call)
        subscriber?.onComplete()
    }

    fun <T> apiCall(call: T, observer: HttpObserver<T>?) {
        observer?.onStart()
        observer?.onNext(call)
        observer?.onComplete()
    }

}