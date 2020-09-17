package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/9/3.
 * 参考rxjava回调，4个监听回调
 */
abstract class ResourceSubscriber<T> {

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    /**
     * 请求开始
     */
    abstract fun onStart()

    /**
     * 取值回调（协程只有成功，失败）
     */
    abstract fun doResult(data: T? = null, throwable: Throwable? = null)

    /**
     * 一个请求完成
     */
    abstract fun onComplete()
    // </editor-fold>

}