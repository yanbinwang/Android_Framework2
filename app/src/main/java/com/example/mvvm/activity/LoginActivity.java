package com.example.mvvm.activity;

import android.text.TextWatcher;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.base.page.PageParams;
import com.example.common.base.proxy.SimpleTextWatcher;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;
import com.example.mvvm.bridge.LoginViewModel;
import com.example.mvvm.databinding.ActivityLoginBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
@Route(path = ARouterPath.LoginActivity)
public class LoginActivity extends BaseTitleActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        super.initView();
        titleBuilder.setTitle("登录").getDefault();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        binding.setClick(new ClickProxy());
        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoModel.observe(this, userInfoModel -> navigation(ARouterPath.UserInfoActivity, new PageParams().append("model", userInfoModel)).finish());
    }

    //点击的绑定（也可直接写在viewmodel中）
    public class ClickProxy implements View.OnClickListener {

        public TextWatcher textWatcher = new SimpleTextWatcher() {

            @Override
            public void onTextChanged(@NotNull CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                binding.btnLogin.setEnabled(!isEmpty(getViewValue(binding.etAccount), getViewValue(binding.etPassword)));
            }
        };

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_login:
                    viewModel.login(getViewValue(binding.etAccount), getViewValue(binding.etPassword));
                    break;
            }
        }

    }

}
