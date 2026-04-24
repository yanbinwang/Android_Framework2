package com.example.gallery.base.bridge

import androidx.lifecycle.LifecycleOwner

/**
 * 所有Presenter层的顶层基类接口
 * 统一规范Presenter的生命周期与回收逻辑
 */
interface BasePresenter : LifecycleOwner, BackHandler

/**
 * 资源释放 & 页面销毁 生命周期接口
 * 所有需要在页面关闭时执行销毁操作的组件都实现该接口
 */
interface BackHandler {

    /**
     * 页面销毁 / 资源释放时调用
     * 用于释放持有的对象、取消网络请求、解绑订阅等操作，避免内存泄漏
     */
    fun navigateBack()

}