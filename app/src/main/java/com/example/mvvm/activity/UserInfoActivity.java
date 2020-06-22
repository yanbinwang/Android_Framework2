package com.example.mvvm.activity;

import android.content.Intent;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.common.utils.ActivityCollector;
import com.example.mvvm.R;
import com.example.mvvm.bridge.UserInfoViewModel;
import com.example.mvvm.databinding.ActivityUserInfoBinding;

/**
 * Created by WangYanBin on 2020/6/3.
 */
@Route(path = ARouterPath.UserInfoActivity)
public class UserInfoActivity extends BaseActivity<UserInfoViewModel, ActivityUserInfoBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_info;
    }

    @Override
    public void initEvent() {
        super.initEvent();
        viewModel.userInfoModel.observe(this, userInfoModel -> binding.setModel(userInfoModel));

        binding.btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.finishAll();
                navigation(ARouterPath.TestListActivity);
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getPageModel();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        LiveDataBus.get().with(LIVEDATA_KEY).postValue(new LiveDataBusEvent(Constants.APP_USER_LOGIN_OUT));
//    }
}
