package com.example.common.base.bridge

import android.app.Activity
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.common.http.HttpCoroutine
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
abstract class BaseViewModel : ViewModel(), LifecycleObserver {
    private var binding: ViewDataBinding? = null//数据绑定类
    private var weakActivity: WeakReference<Activity>? = null//引用的activity
    private var softView: SoftReference<BaseView>? = null//基础UI操作
    private var coroutine = HttpCoroutine()

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(binding: ViewDataBinding?, activity: Activity?, view: BaseView?) {
        this.binding = binding
        this.weakActivity = WeakReference(activity)
        this.softView = SoftReference(view)
    }

    protected fun <VDB : ViewDataBinding> getBinding(): VDB {
        return binding as VDB
    }

    protected fun getCoroutine(): HttpCoroutine {
        return coroutine
    }

    protected fun getActivity(): Activity {
        return weakActivity?.get()!!
    }

    protected fun getContext(): Context {
        return binding?.root?.context!!
    }

    protected fun getView(): BaseView {
        return softView?.get()!!
    }

    override fun onCleared() {
        super.onCleared()
        binding = null
        weakActivity?.clear()
        softView?.clear()
        coroutine.cancel()
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