package com.example.common.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.R;
import com.example.common.base.bridge.BaseImpl;
import com.example.common.base.bridge.BaseView;
import com.example.common.base.bridge.BaseViewModel;
import com.example.common.base.page.PageParams;
import com.example.common.constant.Extras;
import com.example.common.utils.NetWorkUtil;
import com.example.common.widget.dialog.LoadingDialog;
import com.example.common.widget.empty.EmptyLayout;
import com.example.common.widget.xrecyclerview.XRecyclerView;
import com.example.framework.utils.LogUtil;
import com.example.framework.utils.StatusBarUtil;
import com.example.framework.utils.ToastUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by WangYanBin on 2020/6/4.
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements BaseImpl,BaseView {
    protected VM viewModel;
    protected VDB binding;
    protected WeakReference<Activity> activity;//基类activity弱引用
    protected WeakReference<Context> context;//基类context弱引用
    protected StatusBarUtil statusBarUtil;//状态栏工具类
    private LoadingDialog loadingDialog;//刷新球控件，相当于加载动画
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected abstract int getLayoutResID();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initDataBinding(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewModel();
        initView();
        initEvent();
        initData();
    }

    @Override
    public View initDataBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (0 != getLayoutResID()) {
            binding = DataBindingUtil.inflate(inflater, getLayoutResID(), container, false);
            binding.setLifecycleOwner(this);
            return binding.getRoot();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void initViewModel() {
        if (null != binding) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) new ViewModelProvider(this).get(modelClass);
            viewModel.attachView(getActivity(), getContext(), this, binding);
        }
    }

    @Override
    public void initView() {
        activity = new WeakReference<>(getActivity());
        context = new WeakReference<>(getContext());
        statusBarUtil = new StatusBarUtil(activity.get());
        loadingDialog = new LoadingDialog(context.get());
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    @Override
    public void log(String content) {
        LogUtil.INSTANCE.e(TAG, content);
    }

    @Override
    public void showToast(String str) {
        ToastUtil.INSTANCE.mackToastSHORT(str, requireContext().getApplicationContext());
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
    public boolean doResponse(String msg) {
        if (TextUtils.isEmpty(msg)) {
            msg = getString(R.string.label_response_err);
        }
        showToast(!NetWorkUtil.INSTANCE.isNetworkAvailable() ? getString(R.string.label_response_net_err) : msg);
        return true;
    }

    @Override
    public void emptyState(EmptyLayout emptyLayout, String msg) {
        emptyLayout.setVisibility(View.VISIBLE);
        if (doResponse(msg)) {
            emptyLayout.showEmpty();
        }
        if (!NetWorkUtil.INSTANCE.isNetworkAvailable()) {
            emptyLayout.showError();
        }
    }

    @Override
    public void emptyState(XRecyclerView xRecyclerView, String msg, int length) {
        emptyState(xRecyclerView, msg, length, R.mipmap.img_data_empty, EmptyLayout.EMPTY_TXT);
    }

    @Override
    public void emptyState(XRecyclerView xRecyclerView, String msg, int length, int imgInt, String emptyStr) {
        doResponse(msg);
        if (length > 0) {
            return;
        }
        xRecyclerView.setVisibilityEmptyView(View.VISIBLE);
        if (!NetWorkUtil.INSTANCE.isNetworkAvailable()) {
            xRecyclerView.showError();
        } else {
            xRecyclerView.showEmpty(imgInt, emptyStr);
        }
    }

    @Override
    public void openDecor(View view) {
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
    public void setViewFocus(View view) {
        view.setFocusable(true);//设置输入框可聚集
        view.setFocusableInTouchMode(true);//设置触摸聚焦
        view.requestFocus();//请求焦点
        view.findFocus();//获取焦点
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
    // </editor-fold>

}
