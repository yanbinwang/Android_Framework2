package com.example.common.base.bridge

import android.app.Activity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.http.repository.ApiRepository
import com.example.common.http.repository.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(binding: ViewDataBinding?, activity: Activity?, view: BaseView?) {
        this.binding = binding
        this.weakActivity = WeakReference(activity)
        this.softView = SoftReference(view)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch {
            block()
        }

    protected fun getActivity() = weakActivity?.get()

    protected fun getContext() = binding?.root?.context

    protected fun getView() = softView?.get()

    protected fun <VDB : ViewDataBinding> getBinding() = binding as VDB

    protected fun apiMessage(e: Exception) = ApiRepository.apiMessage(e)

    override fun onCleared() {
        super.onCleared()
        binding = null
        weakActivity?.clear()
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