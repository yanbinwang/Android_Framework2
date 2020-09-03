package com.example.common.bus

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


/**
 * author: wyb
 * date: 2019/7/30.
 * 事务管理器，用于添加和清除不同页面订阅的事务
 */
class DisposableManager {
    private val compositeDisposable = CompositeDisposable()

    //单纯的Observables 和 Subscribers管理
    fun add(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    //单个presenter生命周期结束，取消订阅和所有rxbus观察
    fun clear() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

}
