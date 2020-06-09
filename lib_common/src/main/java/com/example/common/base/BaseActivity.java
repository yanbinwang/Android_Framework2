package com.example.common.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.R;
import com.example.common.base.bridge.BaseImpl;
import com.example.common.base.bridge.BaseViewModel;
import com.example.common.bus.LiveDataBus;
import com.example.common.bus.LiveDataBusEvent;
import com.example.common.constant.Constants;
import com.example.common.databinding.ActivityBaseBinding;
import com.example.common.utils.TitleBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的activity和context，以及对对应页面的BaseViewModel中做生命周期的监控
 */
@SuppressWarnings({"unchecked", "Raw"})
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity implements BaseImpl {
    protected VM viewModel;
    protected VDB binding;
    protected TitleBuilder titleBuilder;//标题工具类
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果当前页面有传入布局id，做绑定操作
        if (0 != getLayoutResID()) {
            //绑定的xml作为一个bind持有
            binding = DataBindingUtil.bind(getLayoutInflater().inflate(getLayoutResID(), null));
            binding.setLifecycleOwner(this);
            //根布局包含标题头，将绑定的xml添加进去
            ActivityBaseBinding baseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base);
            baseBinding.flBaseContainer.addView(binding.getRoot());
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) new ViewModelProvider(this).get(modelClass);
            viewModel.attachView(this, this, binding);//注入绑定和上下文
            getLifecycle().addObserver(viewModel);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initView();
        initEvent();
        initData();
    }

    protected abstract int getLayoutResID();

    //控件的事件绑定，请求的回调，页面的跳转完全可交由viewmodel实现
    //此处预留可在activity中操作的方法，如果activity还有要处理的逻辑的话
    @Override
    public void initView() {
        ARouter.getInstance().inject(this);
        titleBuilder = new TitleBuilder(this);
    }

    @Override
    public void initEvent() {
        LiveDataBus.get()
                .with(TAG, LiveDataBusEvent.class)
                .observe(this, event -> {
                    String action = event.getAction();
                    switch (action) {
                        //注销登出
                        case Constants.APP_USER_LOGIN_OUT:
                            if (!"mainactivity".equals(TAG)) {
                                finish();
                            }
                            break;
                        //切换语言
                        case Constants.APP_SWITCH_LANGUAGE:
                            finish();
                            break;
                    }
                });
    }

    @Override
    public void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

}
