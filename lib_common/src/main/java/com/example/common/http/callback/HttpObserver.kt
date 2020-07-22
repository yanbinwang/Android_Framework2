package com.example.common.http.callback

import androidx.lifecycle.Observer

/**
 * Created by WangYanBin on 2020/6/8.
 * 针对网络请求回调的解析处理
 */
abstract class HttpObserver<T> : Observer<ApiResponse<T>?> {

    constructor() {
        onStart()
    }

    override fun onChanged(apiResponse: ApiResponse<T>?) {
        onNext(apiResponse)
    }

    protected abstract fun onStart()

    protected abstract fun onNext(apiResponse: ApiResponse<T>?)

    protected abstract fun onComplete()

}