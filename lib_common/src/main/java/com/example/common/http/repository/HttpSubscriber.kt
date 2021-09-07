package com.example.common.http.repository

/**
 * Created by WangYanBin on 2020/9/3.
 * 继承原先的监听后对结果做处理（用于项目中的接口请求，可不去实现成功和失败）
 * onSuccess->成功直接将外层剥离取得对象T
 * onFailed->失败返回失败异常，或服务器给予的失败提示文案
 */
abstract class HttpSubscriber<T> : ResourceSubscriber<ApiResponse<T>>() {

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    final override fun onNext(t: ApiResponse<T>?) {
        if (null != t) {
            val msg = t.msg
            val code = t.code
            if (0 == code) {
                onSuccess(t.data)
            } else {
                onFailed(Exception(), msg)
            }
        } else {
            onFailed(Exception(), "")
        }
    }

    final override fun onError(throwable: Throwable?) {
        super.onError(throwable)
        onFailed(throwable, "")
    }
    // </editor-fold>

    /**
     * 请求成功，直接回调对象
     */
    open fun onSuccess(data: T?) {}

    /**
     * 请求失败，获取失败原因
     */
    @JvmOverloads
    open fun onFailed(e: Throwable?, msg: String?, code: Int? = -1) {}

}