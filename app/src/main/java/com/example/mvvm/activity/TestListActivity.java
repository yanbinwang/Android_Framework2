package com.example.mvvm.activity;

import com.example.common.base.BaseActivity;
import com.example.mvvm.R;
import com.example.mvvm.bridge.TestListViewModel;
import com.example.mvvm.databinding.ActivityTestListBinding;

/**
 * Created by WangYanBin on 2020/6/4.
 */
public class TestListActivity extends BaseActivity<TestListViewModel,ActivityTestListBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_test_list;
    }

    @Override
    public void initView() {
        super.initView();
        binding.setVm(viewModel);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getListData();
    }

}
