package com.example.common.base.bridge;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.BaseApplication;
import com.example.common.R;
import com.example.common.base.page.PageParams;
import com.example.common.bus.LiveDataBus;
import com.example.common.bus.LiveDataBusEvent;
import com.example.common.constant.Constants;
import com.example.common.constant.Extras;
import com.example.common.utils.TitleBuilder;
import com.example.common.utils.permission.AndPermissionUtil;
import com.example.common.widget.dialog.LoadingDialog;
import com.example.framework.utils.LogUtil;
import com.example.framework.utils.StatusBarUtil;
import com.example.framework.utils.ToastUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 */
public abstract class BaseViewModel extends ViewModel implements BaseImpl, BaseView, LifecycleObserver {
    protected WeakReference<Activity> activity;
    protected WeakReference<Context> context;
    protected StatusBarUtil statusBarUtil;//状态栏工具类
    protected AndPermissionUtil andPermissionUtil;//获取权限类
    private ViewDataBinding binding;
    private CountDownTimer countDownTimer;//计时器
    private LoadingDialog loadingDialog;//刷新球控件，相当于加载动画
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    //binding在注入的时候通过泛型取得对应页面产生的binding，在基类中注入对应的bind进行强转，
    //这样在继承baseviewmodel中就可以直接取得对应的binding，不需要强转
    public void attachView(Activity activity, Context context, ViewDataBinding binding) {
        this.activity = new WeakReference<>(activity);
        this.context = new WeakReference<>(context);
        this.binding = binding;
    }

    protected <VDB extends ViewDataBinding> VDB getBinding() {
        return (VDB) binding;
    }

    @Override
    public void initView() {
        statusBarUtil = new StatusBarUtil(activity.get());
        loadingDialog = new LoadingDialog(context.get());
        andPermissionUtil = new AndPermissionUtil(context.get());
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    public void log(String msg) {
        LogUtil.INSTANCE.e(TAG, msg);
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.INSTANCE.mackToastSHORT(msg, BaseApplication.getInstance().getApplicationContext());
    }

    @Override
    public void showDialog() {
        showDialog(false);
    }

    @Override
    public void showDialog(boolean isClose) {
        loadingDialog.show(isClose);
    }

    @Override
    public void hideDialog() {
        loadingDialog.hide();
    }

    @NotNull
    @Override
    public Activity navigation(String path) {
        return navigation(path, null);
    }

    @Override
    public Activity navigation(String path, PageParams pageParams) {
        Postcard postcard = ARouter.getInstance().build(path);
        Integer code = null;
        if (pageParams != null) {
            Map<String, Object> map = pageParams.getParams();
            for (String key : map.keySet()) {
                Object value = map.get(key);
                Class<?> cls = value.getClass();
                if (key.equals(Extras.REQUEST_CODE)) {
                    code = (Integer) value;
                    continue;
                }
                if (cls == String.class) {
                    postcard.withString(key, (String) value);
                } else if (value instanceof Parcelable) {
                    postcard.withParcelable(key, (Parcelable) value);
                } else if (value instanceof Serializable) {
                    postcard.withSerializable(key, (Serializable) value);
                } else if (cls == int.class) {
                    postcard.withInt(key, (int) value);
                } else if (cls == long.class) {
                    postcard.withLong(key, (long) value);
                } else if (cls == boolean.class) {
                    postcard.withBoolean(key, (boolean) value);
                } else if (cls == float.class) {
                    postcard.withFloat(key, (float) value);
                } else if (cls == double.class) {
                    postcard.withDouble(key, (double) value);
                } else if (cls == char[].class) {
                    postcard.withCharArray(key, (char[]) value);
                } else if (cls == Bundle.class) {
                    postcard.withBundle(key, (Bundle) value);
                } else {
                    throw new RuntimeException("不支持参数类型" + ": " + cls.getSimpleName());
                }
            }
        }
        if (code == null) {
            postcard.navigation();
        } else {
            postcard.navigation(activity.get(), code);
        }
        return activity.get();
    }

    @Override
    public void openDecor(@Nullable View view) {
        closeDecor();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((InputMethodManager) activity.get().getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0,
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity.get().getSystemService(INPUT_METHOD_SERVICE);
            inputmanger.showSoftInput(view, 2);
        }
    }

    @Override
    public void closeDecor() {
        View decorView = activity.get().getWindow().peekDecorView();
        // 隐藏软键盘
        if (decorView != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity.get().getSystemService(INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(decorView.getWindowToken(), 0);
        }
    }

    @Override
    public String getViewValue(@Nullable View view) {
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString().trim();
        } else if (view instanceof TextView) {
            return ((TextView) view).getText().toString().trim();
        } else if (view instanceof CheckBox) {
            return ((CheckBox) view).getText().toString().trim();
        } else if (view instanceof RadioButton) {
            return ((RadioButton) view).getText().toString().trim();
        } else if (view instanceof Button) {
            return ((Button) view).getText().toString().trim();
        }
        return null;
    }

    @Override
    public void setViewFocus(@Nullable View view) {
        view.setFocusable(true);//设置输入框可聚集
        view.setFocusableInTouchMode(true);//设置触摸聚焦
        view.requestFocus();//请求焦点
        view.findFocus();//获取焦点
    }

    @Override
    public void VISIBLE(@Nullable View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void INVISIBLE(@Nullable View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void GONE(@Nullable View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean isEmpty(@Nullable Object... objs) {
        for (Object obj : objs) {
            if (obj == null) {
                return true;
            } else if (obj instanceof String && obj.equals("")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String processedString(@Nullable String source, @Nullable String defaultStr) {
        if (source == null) {
            return defaultStr;
        } else {
            if (source.trim().isEmpty()) {
                return defaultStr;
            } else {
                return source;
            }
        }
    }

    @Override
    public void setDownTime(@Nullable TextView txt) {
        setDownTime(txt, ContextCompat.getColor(context.get(), R.color.gray_9f9f9f), ContextCompat.getColor(context.get(), R.color.gray_9f9f9f));
    }

    @Override
    public void setDownTime(@Nullable TextView txt, int startColorId, int endColorId) {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    txt.setText(MessageFormat.format("{0}s后重新获取", millisUntilFinished / 1000));// 剩余多少毫秒
                    txt.setTextColor(startColorId);
                    txt.setEnabled(false);
                }

                @Override
                public void onFinish() {
                    txt.setEnabled(true);
                    txt.setTextColor(endColorId);
                    txt.setText("重新发送");
                }
            };
        }
        countDownTimer.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initView();
        initEvent();
        initData();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        log("onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        log("onResume");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        log("onPause");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        log("onStop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        log("onDestroy");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onAny() {
        log("onAny");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        log("onCleared");
    }
    // </editor-fold>

}
