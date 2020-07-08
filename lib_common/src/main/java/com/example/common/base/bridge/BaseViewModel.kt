package com.example.common.base.bridge

import android.content.Context
import androidx.lifecycle.*
import com.example.common.BaseApplication
import java.lang.ref.SoftReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 */
abstract class BaseViewModel : AndroidViewModel(BaseApplication.instance), LifecycleObserver {
    private var view: SoftReference<BaseView>? = null//基础UI操作
    private var owner: LifecycleOwner? = null//被观察者

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun attachView(view: BaseView, owner: LifecycleOwner) {
        this.view = SoftReference(view)
        this.owner = owner
    }

    protected fun getContext(): Context {
        return getApplication<BaseApplication>().applicationContext
    }

    protected fun getView(): BaseView {
        return view?.get()!!
    }

    protected fun getOwner(): LifecycleOwner {
        return owner!!
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
//        getView().log("onCreate");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
//        getView().log("onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
//        getView().log("onResume");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
//        getView().log("onPause");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
//        getView().log("onStop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
//        getView().log("onDestroy");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAny() {
//        getView().log("onAny");
    }

    override fun onCleared() {
        super.onCleared()
        view!!.clear()
        owner = null
    }
    // </editor-fold>

}