package com.example.mvvm.activity;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;
import com.example.mvvm.bridge.MainViewModel;
import com.example.mvvm.databinding.ActivityMainBinding;

/**
 * Mvvm中，activity的代码量应该非常少，它并不和数据以及控件直接交互
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
        binding.setViewModel(viewModel);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        binding.setClick(new ClickProxy());
    }

    public class ClickProxy {

        public void toLoginPage() {
            navigation(ARouterPath.LoginActivity);
        }

        public void toListPage() {
            navigation(ARouterPath.TestListActivity);
        }

    }

}