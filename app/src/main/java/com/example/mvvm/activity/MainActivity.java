package com.example.mvvm.activity;


import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;
import com.example.mvvm.bridge.MainViewModel;
import com.example.mvvm.databinding.ActivityMainBinding;

/**
 * Mvvm中，activity的代码量应该非常少，它并不和数据以及控件直接交互
 * 不牵扯处理逻辑的放在activity，其余的都丢给viewmodel，然后通过livedata回调操作
 * 凡是牵扯需要逻辑的方法，以及从任何入口进去到Controller的数据
 * （上一个类传过来的数据，网络请求获取的数据等等）都需要放在ViewModel中。
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

    @Override
    public void initEvent() {
        super.initEvent();
        binding.setClick(new ClickProxy());
    }

    public class ClickProxy implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_login:
                    navigation(ARouterPath.LoginActivity);
                    break;
                case R.id.btn_list:
                    navigation(ARouterPath.TestListActivity);
                    break;
            }
        }
    }

}