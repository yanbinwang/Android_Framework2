package com.example.mvvm.activity;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;
import com.example.mvvm.bridge.MainViewModel;
import com.example.mvvm.databinding.ActivityMainBinding;

/**
 * Mvvm中，activity的代码量应该非常少，它并不和数据以及控件直接交互
 * 涉及到页面中点击触发viewmodel操作，主动发起网络请求等这类事件，写在activity中。
 * 纯点击事件写在viewmodel中,设计view的属性设置逻辑写在activity中
 */
@Route(path = ARouterPath.MainActivity)
public class MainActivity extends BaseTitleActivity<MainViewModel, ActivityMainBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        super.initView();
        titleBuilder.setTitle("10086").getDefault();
    }

}