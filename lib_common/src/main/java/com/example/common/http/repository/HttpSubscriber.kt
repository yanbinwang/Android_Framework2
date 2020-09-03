package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/9/3.
 */
interface HttpSubscriber<T> {

    /**
     * 请求成功，直接回调对象
     */
    fun onSuccess(data: T?)

    /**
     * 请求失败，获取失败原因
     */
    fun onFailed(e: Throwable?, msg: String?)

}