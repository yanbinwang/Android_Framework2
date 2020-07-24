package com.example.common.base.bridge

import android.content.Context
import androidx.lifecycle.*
import com.example.common.BaseApplication
import com.example.common.http.callback.ApiResponse
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
    private var view: SoftReference<BaseView>? = null//基础UI操作
    private var owner: LifecycleOwner? = null//被观察者

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(view: BaseView, owner: LifecycleOwner) {
        this.view = SoftReference(view)
        this.owner = owner
    }

    protected fun <T> addDisposable(liveData: LiveData<ApiResponse<T>>, subscriber: HttpSubscriber<T>) {
        liveData.observe(getOwner(), subscriber)
    }

    protected fun getContext(): Context {
        return getApplication<BaseApplication>().applicationContext
    }

    protected fun getView(): BaseView {
        return view?.get()!!
    }

    private fun getOwner(): LifecycleOwner {
        return owner!!
    }

    override fun onCleared() {
        super.onCleared()
        view!!.clear()
        owner = null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAny() {
    }
    // </editor-fold>

}