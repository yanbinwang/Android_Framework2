package com.example.common.base.bridge

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.common.BaseApplication
import com.example.common.http.callback.HttpObserver
import com.lcodecore.tkrefreshlayout.utils.LogUtil
import kotlinx.coroutines.*
import java.lang.ref.SoftReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
abstract class BaseViewModel : ViewModel(), LifecycleObserver {
    private var view: SoftReference<BaseView>? = null//基础UI操作
    private val viewModelJob = SupervisorJob()//此ViewModel运行的所有协程所用的任务,终止这个任务将终止此ViewModel开始的所有协程
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)//所有协程的主作用域

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun attachView(view: BaseView) {
        this.view = SoftReference(view)
    }

    protected fun <T> observe(t: T, subscriber: HttpObserver<T>) {
        subscriber.onStart()
        subscriber.onNext(t)
        subscriber.onComplete()
    }

    protected fun getUiScope(): CoroutineScope {
        return uiScope
    }

    protected fun getContext(): Context {
        return BaseApplication.instance?.applicationContext!!
    }

    protected fun getView(): BaseView {
        return view?.get()!!
    }

    override fun onCleared() {
        super.onCleared()
        view?.clear()
        viewModelJob.cancel()
    }
    // </editor-fold>

//    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun onCreate() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun onStart() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun onResume() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun onPause() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun onStop() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
//    fun onAny() {
//    }
//    // </editor-fold>

}