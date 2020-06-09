package com.example.mvvm.activity;


import com.example.common.base.BaseActivity;
import com.example.mvvm.R;
import com.example.mvvm.bridge.MainViewModel;
import com.example.mvvm.databinding.ActivityMainBinding;

/**
 * Mvvm中，activity的代码量应该非常少，它并不和数据以及控件直接交互
 */
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        super.initView();
        titleBuilder.setTitle("10086").getDefault();
        binding.setVm(viewModel);//控制器作为一个中间层，让viewmodel直接和数据交互，本身不再做繁复的逻辑处理
    }

}