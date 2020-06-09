package com.example.mvvm.activity;

import com.example.common.base.BaseActivity;
import com.example.mvvm.R;
import com.example.mvvm.bridge.LoginViewModel;
import com.example.mvvm.databinding.ActivityLoginBinding;

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    public void initData() {
        super.initData();
        binding.setVm(viewModel);
    }

}
