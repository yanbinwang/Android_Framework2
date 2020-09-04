package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/9/3.
 */
interface ResourceSubscriber<T> {

    fun onStart()

    fun onNext(t: T?)

    fun onComplete()

}