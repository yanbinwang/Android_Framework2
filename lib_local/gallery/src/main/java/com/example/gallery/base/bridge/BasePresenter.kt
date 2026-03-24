package com.example.gallery.base.bridge

import androidx.lifecycle.LifecycleOwner

/**
 * 所有Presenter层的顶层基类接口
 * 统一规范Presenter的生命周期与回收逻辑
 */
interface BasePresenter : LifecycleOwner, Bye