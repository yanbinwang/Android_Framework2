package com.example.common.base.bridge

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，Binding文件，开发的时候可以随时存取和调用基类Activity的控件和方法
 */
abstract class BaseViewModel : ViewModel(), LifecycleObserver {
    private var activity: WeakReference<Activity>? = null
    private var context: WeakReference<Context>? = null
    private var view: SoftReference<BaseView>? = null
    private var owner: LifecycleOwner? = null

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun attachView(activity: Activity, context: Context, view: BaseView, owner: LifecycleOwner) {
        this.activity = WeakReference(activity)
        this.context = WeakReference(context)
        this.view = SoftReference(view)
        this.owner = owner
    }

    protected fun getActivity(): Activity {
        return activity?.get()!!
    }

    protected fun getContext(): Context {
        return context?.get()!!
    }

    protected fun getView(): BaseView {
        return view?.get()!!
    }

    protected fun getOwner(): LifecycleOwner {
        return owner!!
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    //    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    //    public void onCreate() {
    //        getView()?.log("onCreate");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    //    public void onStart() {
    //        getView()?.log("onStart");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    //    public void onResume() {
    //        getView()?.log("onResume");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    //    public void onPause() {
    //        getView()?.log("onPause");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    //    public void onStop() {
    //        getView()?.log("onStop");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    //    public void onDestroy() {
    //        getView()?.log("onDestroy");
    //    }
    //
    //    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    //    public void onAny() {
    //        getView()?.log("onAny");
    //    }
    //
    override fun onCleared() {
        super.onCleared()
        activity!!.clear()
        context!!.clear()
        view!!.clear()
        owner = null
    }
    // </editor-fold>

}