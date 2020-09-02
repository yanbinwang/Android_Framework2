package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/8/27.
 * 用于框架内网络请求
 */
object HttpCall {

    fun <T> apiCall(t: ApiResponse<T>, subscriber: HttpSubscriber<T>?) {
        apiCall(t, object : HttpObserver<ApiResponse<T>> {

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

    fun <T> apiCall(t: T, subscriber: HttpObserver<T>?) {
        subscriber?.onStart()
        subscriber?.onNext(t)
        subscriber?.onComplete()
    }

}