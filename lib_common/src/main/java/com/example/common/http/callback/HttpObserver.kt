package com.example.common.http.callback

import androidx.lifecycle.Observer

/**
 * Created by WangYanBin on 2020/6/8.
 * 针对网络请求回调的解析处理，由于livedata本身并没有整体的生命周期监听，故手动封装了3个回调
 * 如果是框架内约定的格式，可以使用其子类HttpSubscriber，如果是访问外部地址，则使用该类
 */
abstract class HttpObserver<T> : Observer<T?> {

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        onStart()
    }

    override fun onChanged(t: T?) {
        onNext(t)
        onComplete()
    }
    // </editor-fold>

    /**
     * 构造开始
     */
    protected abstract fun onStart()

    /**
     * 解析回调（retrofit2中不管成功失败都会回调）
     */
    protected abstract fun onNext(t: T?)

    /**
     * 完成回调
     */
    protected abstract fun onComplete()

}