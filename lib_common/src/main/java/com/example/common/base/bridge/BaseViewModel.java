package com.example.common.base.bridge;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.example.common.BR;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入对应的View操作，对应的Binding文件，开发的时候可以随时存取和调用基类Activity的控件和方法
 */
public abstract class BaseViewModel extends ViewModel implements LifecycleObserver {
    protected WeakReference<Activity> activity;
    protected WeakReference<Context> context;
    protected SoftReference<BaseView> view;
    private ViewDataBinding binding;
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    //binding在注入的时候通过泛型取得对应页面产生的binding，在基类中注入对应的bind进行强转，
    //这样在继承baseviewmodel中就可以直接取得对应的binding，不需要强转
    public void attachView(Activity activity, Context context, BaseView view, ViewDataBinding binding) {
        binding.setVariable(BR._all, this);
        this.activity = new WeakReference<>(activity);
        this.context = new WeakReference<>(context);
        this.view = new SoftReference<>(view);
        this.binding = binding;
    }

    protected <VDB extends ViewDataBinding> VDB getBinding() {
        return (VDB) binding;
    }

    protected LifecycleOwner getOwner() {
        return binding.getLifecycleOwner();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.e(TAG, "onCreate");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        view.get().log("onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        view.get().log("onResume");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        view.get().log("onPause");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        view.get().log("onStop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        view.get().log("onDestroy");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onAny() {
        view.get().log("onAny");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        view.get().log("onCleared");
        activity.clear();
        context.clear();
        view.clear();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

}
