package com.example.common.base.bridge;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.lang.ref.WeakReference;

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 */
public abstract class BaseViewModel extends ViewModel implements LifecycleObserver {
    protected WeakReference<Activity> activity;
    protected WeakReference<Context> context;
    protected BaseView view;
    private ViewDataBinding binding;
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    //binding在注入的时候通过泛型取得对应页面产生的binding，在基类中注入对应的bind进行强转，
    //这样在继承baseviewmodel中就可以直接取得对应的binding，不需要强转
    public void attachView(Activity activity, Context context, BaseView view, ViewDataBinding binding) {
        this.activity = new WeakReference<>(activity);
        this.context = new WeakReference<>(context);
        this.view = view;
        this.binding = binding;
    }

    protected <VDB extends ViewDataBinding> VDB getBinding() {
        return (VDB) binding;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.e(TAG, "onCreate");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        view.log("onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        view.log("onResume");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        view.log("onPause");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        view.log("onStop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        view.log("onDestroy");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onAny() {
        view.log("onAny");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        view.log("onCleared");
        view = null;
        activity.clear();
        context.clear();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

}
