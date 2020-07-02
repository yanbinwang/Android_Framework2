package com.example.mvvm.activity;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.BR;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.common.constant.Constants;
import com.example.framework.utils.lifecycle.LiveDataBus;
import com.example.mvvm.R;
import com.example.mvvm.databinding.ActivityUserInfoBinding;

/**
 * Created by WangYanBin on 2020/6/3.
 * ViewModel可不写，但是binding必须传
 */
@Route(path = ARouterPath.UserInfoActivity)
public class UserInfoActivity extends BaseActivity<ActivityUserInfoBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_info;
    }

    @Override
    public void initView() {
        super.initView();
        binding.setVariable(BR.model, getIntent().getSerializableExtra("model"));
        binding.setVariable(BR.event, new PageEvent());
    }

    public class PageEvent {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_test:
//                    ActivityCollector.finishAll();
//                    navigation(ARouterPath.TestListActivity);
                    //发送消息
                    LiveDataBus.get().with(Constants.APP_USER_LOGIN_OUT).postValue("50998");
                    finish();
                    break;
            }
        }

    }

}
