package com.example.common.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.base.utils.LogUtil;
import com.example.base.utils.ToastUtil;
import com.example.common.base.bridge.BaseImpl;
import com.example.common.base.bridge.BaseView;
import com.example.common.base.bridge.BaseViewModel;
import com.example.common.base.page.PageParams;
import com.example.common.base.proxy.SimpleTextWatcher;
import com.example.common.constant.Extras;
import com.example.common.utils.builder.StatusBarBuilder;
import com.example.common.widget.dialog.LoadingDialog;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的Activity和Context
 */
@SuppressWarnings({"Raw"})
public abstract class BaseActivity<VDB extends ViewDataBinding> extends AppCompatActivity implements BaseImpl, BaseView {
    protected VDB binding;
    protected WeakReference<Activity> activity;//基类activity弱引用
    protected WeakReference<Context> context;//基类context弱引用
    protected StatusBarBuilder statusBarBuilder;//状态栏工具类
    private BaseViewModel baseViewModel;//数据模型
    private LoadingDialog loadingDialog;//刷新球控件，相当于加载动画
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initView();
        initEvent();
        initData();
    }

    protected abstract int getLayoutResID();

    protected <VM extends BaseViewModel> VM createViewModel(Class<VM> vmClass) {
        if (null == baseViewModel) {
            baseViewModel = new ViewModelProvider(this).get(vmClass);
            baseViewModel.attachView(this);
            getLifecycle().addObserver(baseViewModel);
        }
        return (VM) baseViewModel;
    }

    //控件的事件绑定，请求的回调，页面的跳转完全可交由viewmodel实现
    @Override
    public void initView() {
        ARouter.getInstance().inject(this);
        if (0 != getLayoutResID()) {
            binding = DataBindingUtil.setContentView(this, getLayoutResID());
            binding.setLifecycleOwner(this);
        }
        activity = new WeakReference<>(this);
        context = new WeakReference<>(this);
        statusBarBuilder = new StatusBarBuilder(activity.get());
        loadingDialog = new LoadingDialog(context.get());
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    public boolean isEmpty(Object... objs) {
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
    public void openDecor(View view) {
        closeDecor();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0,
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.showSoftInput(view, 2);
        }
    }

    @Override
    public void closeDecor() {
        View decorView = getWindow().peekDecorView();
        // 隐藏软键盘
        if (decorView != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(decorView.getWindowToken(), 0);
        }
    }

    @Override
    public void getFocus(View view) {
        view.setFocusable(true);//设置输入框可聚集
        view.setFocusableInTouchMode(true);//设置触摸聚焦
        view.requestFocus();//请求焦点
        view.findFocus();//获取焦点
    }

    @Override
    public String getParameters(View view) {
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
    public void onTextChanged(SimpleTextWatcher simpleTextWatcher, View... views) {
        for (View view : views) {
            if (view instanceof EditText) {
                ((EditText) view).addTextChangedListener(simpleTextWatcher);
            }
        }
    }

    @Override
    public void onClick(View.OnClickListener onClickListener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(onClickListener);
            }
        }
    }

    @Override
    public void VISIBLE(View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void INVISIBLE(View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void GONE(View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    @Override
    public void log(String content) {
        LogUtil.e(TAG, content);
    }

    @Override
    public void showToast(String str) {
        ToastUtil.mackToastSHORT(str, getApplicationContext());
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
            postcard.navigation(this, code);
        }
        return this;
    }
    // </editor-fold>

}