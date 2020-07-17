package com.example.common.http.callback

import androidx.lifecycle.Observer
import com.example.common.http.ResponseBody

/**
 * Created by WangYanBin on 2020/6/8.
 * 针对网络请求回调的解析处理
 */
abstract class HttpObserver<T> : Observer<ResponseBody<T>?> {

    constructor() {
        onStart()
    }

    override fun onChanged(responseBody: ResponseBody<T>?) {
        onNext(responseBody)
    }

    protected abstract fun onStart()

    protected abstract fun onNext(responseBody: ResponseBody<T>?)

    protected abstract fun onComplete()

}