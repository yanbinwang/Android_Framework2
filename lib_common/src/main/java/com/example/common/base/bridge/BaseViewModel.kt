package com.example.common.base.bridge

import android.app.Activity
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.common.BR
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，Binding文件，开发的时候可以随时存取和调用基类Activity的控件和方法
 */
abstract class BaseViewModel<VDB : ViewDataBinding?> : ViewModel(), LifecycleObserver {
    protected var activity: WeakReference<Activity>? = null
    protected var context: WeakReference<Context>? = null
    protected var view: SoftReference<BaseView>? = null
    protected var binding: VDB? = null

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun attachView(activity: Activity, context: Context, view: BaseView, binding: VDB) {
        this.activity = WeakReference(activity)
        this.context = WeakReference(context)
        this.view = SoftReference(view)
        this.binding = binding
        this.binding?.setVariable(BR._all, this)
    }
    //    public VDB getBinding() {
    //        return binding;
    //    }
    //
    //    protected LifecycleOwner getOwner() {
    //        return binding.getLifecycleOwner();
    //    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    //    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    //    public void onCreate() {
    //        view.get().log("onCreate");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    //    public void onStart() {
    //        view.get().log("onStart");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    //    public void onResume() {
    //        view.get().log("onResume");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    //    public void onPause() {
    //        view.get().log("onPause");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    //    public void onStop() {
    //        view.get().log("onStop");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    //    public void onDestroy() {
    //        view.get().log("onDestroy");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    //    public void onAny() {
    //        view.get().log("onAny");
    //    }
    //
    override fun onCleared() {
        super.onCleared()
        activity!!.clear()
        context!!.clear()
        view!!.clear()
        if (binding != null) {
            binding?.unbind()
        }
    }
    // </editor-fold>

}