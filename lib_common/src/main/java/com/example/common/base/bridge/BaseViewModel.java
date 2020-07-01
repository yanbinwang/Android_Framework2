package com.example.common.base.bridge;

import android.app.Activity;
import android.content.Context;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.example.common.BR;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，Binding文件，开发的时候可以随时存取和调用基类Activity的控件和方法
 */
public abstract class BaseViewModel<VDB extends ViewDataBinding> extends ViewModel implements LifecycleObserver {
    protected WeakReference<Activity> activity;
    protected WeakReference<Context> context;
    protected SoftReference<BaseView> view;
    protected VDB binding;

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    //传入基类生成的binding，在ViewModel中建立关联，让被关联的ViewModel可以获取到binding和对应binding的LifecycleOwner
    public void attachView(Activity activity, Context context, BaseView view, VDB binding) {
        this.activity = new WeakReference<>(activity);
        this.context = new WeakReference<>(context);
        this.view = new SoftReference<>(view);
        this.binding = binding;
        this.binding.setVariable(BR._all, this);
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
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    protected void onAny() {
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        activity.clear();
        context.clear();
        view.clear();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

}
