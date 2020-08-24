package com.example.common.http.callback

/**
 * Created by WangYanBin on 2020/6/8.
 * 针对网络请求回调的解析处理，由于livedata本身并没有整体的生命周期监听，故手动封装了3个回调
 * 如果是框架内约定的格式，可以使用其子类HttpSubscriber，如果是访问外部地址，则使用该类
 */
interface HttpObserver<T> {

    /**
     * 构造开始
     */
    fun onStart()

    /**
     * 解析回调（retrofit2中不管成功失败都会回调）
     */
    fun onNext(t: T?)

    /**
     * 完成回调
     */
    fun onComplete()

}