package com.example.mvvm.activity;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.framework.utils.StringUtil;
import com.example.mvvm.R;
import com.example.mvvm.bridge.LoginViewModel;
import com.example.mvvm.databinding.ActivityLoginBinding;

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
@Route(path = ARouterPath.LoginActivity)
public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        super.initView();
        binding.setVm(viewModel);
        binding.setClick(new ClickProxy());
    }

    @Override
    public void initEvent() {
        super.initEvent();
        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.token.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });
    }

    //点击的绑定（也可直接写在viewmodel中）
    public class ClickProxy {

        public void toLogin() {
            viewModel.login(StringUtil.INSTANCE.getViewValue(binding.etAccount), StringUtil.INSTANCE.getViewValue(binding.etPassword));
        }

    }

}
