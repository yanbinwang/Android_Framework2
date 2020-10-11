package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/9/3.
 * 参考rxjava回调，3个监听回调
 * onStart->开始请求
 * onResult->携程只有成功和失败，try和catch，故直接合并为onResult，对象为空就是失败，且外层要有约定的编码
 * onComplete->请求完成
 */
abstract class ResourceSubscriber<T> {

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    /**
     * 请求开始
     */
    protected open fun onStart() {}

    /**
     * 取值回调（协程只有成功，失败）
     */
    protected open fun onNext(t: T? = null) {}

    /**
     * 请求异常
     */
    protected open fun onError(throwable: Throwable? = null) {}

    /**
     * 一个请求完成
     */
    protected open fun onComplete() {}
    // </editor-fold>

}