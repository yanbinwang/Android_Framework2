package com.example.mvvm.activity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
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
    public void initView() {
        super.initView();
        binding.setVm(viewModel);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getPageModel();
    }

}
