package com.example.common.base.bridge

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import com.example.common.BaseApplication
import com.example.common.http.callback.ApiResponse
import com.example.common.http.callback.HttpObserver
import com.example.common.http.callback.HttpSubscriber
import java.lang.ref.SoftReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 * LifecycleOwner->获取被观察者
 */
abstract class BaseViewModel : AndroidViewModel(BaseApplication.instance), LifecycleObserver {
    private var binding: ViewDataBinding? = null//数据绑定类
    private var view: SoftReference<BaseView>? = null//基础UI操作

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(binding: ViewDataBinding, view: BaseView) {
        this.binding = binding
        this.view = SoftReference(view)
    }

    protected fun <T> observe(liveData: LiveData<ApiResponse<T>>, subscriber: HttpSubscriber<T>) {
        liveData.observe(binding?.lifecycleOwner!!, subscriber)
    }

    protected fun <T> observe(liveData: LiveData<T>, observer: HttpObserver<T>) {
        liveData.observe(binding?.lifecycleOwner!!, observer)
    }

    protected fun getContext(): Context {
        return getApplication<BaseApplication>().applicationContext
    }

    protected fun getView(): BaseView {
        return view?.get()!!
    }

    protected fun <VDB : ViewDataBinding> getBinding(): VDB {
        return binding as VDB
    }

    override fun onCleared() {
        super.onCleared()
        binding = null
        view?.clear()
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