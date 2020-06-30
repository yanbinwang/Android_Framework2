package com.example.mvvm.activity;

import android.text.TextWatcher;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.base.page.PageParams;
import com.example.common.base.proxy.SimpleTextWatcher;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.BR;
import com.example.mvvm.R;
import com.example.mvvm.bridge.LoginViewModel;
import com.example.mvvm.databinding.ActivityLoginBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
@Route(path = ARouterPath.LoginActivity)
public class LoginActivity extends BaseTitleActivity<ActivityLoginBinding> {
    private LoginViewModel viewModel;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        super.initView();
        viewModel = getViewModel(LoginViewModel.class);
        titleBuilder.setTitle("登录").getDefault();
        binding.setVariable(BR.event, new PageEvent());
    }

    @Override
    public void initEvent() {
        super.initEvent();
        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoModel.observe(this, userInfoModel -> navigation(ARouterPath.UserInfoActivity, new PageParams().append("model", userInfoModel)));
//        viewModel.getData();
//        for (int i = 0; i < 1000; i++) {
//            int position = i;
//            log("当前第" + position + "个请求开始！");
////            LiveDataBus.BusMutableLiveData x = new LiveDataBus.BusMutableLiveData();
//            BaseSubscribe.INSTANCE
//                    .getTestApi()
//                    .observe(this,new HttpSubscriber<Object>() {
//                        @Override
//                        protected void onSuccess(Object data) {
//
//                        }
//
//                        @Override
//                        protected void onFailed(String msg) {
//                        }
//
//                        @Override
//                        protected void onFinish() {
//                            log("当前第" + position + "个请求结束！");
//                        }
//                    });
//        }
    }

    //点击的绑定（也可直接写在viewmodel中）
    public class PageEvent {

        public TextWatcher textWatcher = new SimpleTextWatcher() {

            @Override
            public void onTextChanged(@NotNull CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                binding.btnLogin.setEnabled(!isEmpty(getViewValue(binding.etAccount), getViewValue(binding.etPassword)));
            }
        };

        public View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.btn_login:
                    viewModel.login(getViewValue(binding.etAccount), getViewValue(binding.etPassword));
                    break;
            }
        };

    }

}
