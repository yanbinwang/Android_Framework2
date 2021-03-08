package com.example.common.base.bridge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.common.widget.empty.EmptyLayout
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
@SuppressLint("StaticFieldLeak")
abstract class BaseViewModel : ViewModel(), LifecycleObserver {
    private var weakActivity: WeakReference<Activity>? = null//引用的activity
    private var weakContext: WeakReference<Context>? = null//引用的context
    private var softView: SoftReference<BaseView>? = null//基础UI操作
    private var emptyLayout: EmptyLayout? = null

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(activity: Activity?, context: Context?, view: BaseView?) {
        this.weakActivity = WeakReference(activity)
        this.weakContext = WeakReference(context)
        this.softView = SoftReference(view)
    }

    protected fun addEmptyLayout(emptyLayout: EmptyLayout?) {
        this.emptyLayout = emptyLayout
        showEmptyLayout()
    }

    protected fun showEmptyLayout() {
        emptyLayout?.showLoading()
    }

    protected fun hideEmptyLayout() {
        emptyLayout?.visibility = View.GONE
    }

    protected fun getActivity() = weakActivity?.get()

    protected fun getContext() = weakContext?.get()

    protected fun getView() = softView?.get()

    override fun onCleared() {
        super.onCleared()
        weakActivity?.clear()
        weakContext?.clear()
        softView?.clear()
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