package com.example.mvvm.activity;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.BR;
import com.example.mvvm.R;
import com.example.mvvm.adapter.TestListAdapter;
import com.example.mvvm.bridge.TestListViewModel;
import com.example.mvvm.databinding.ActivityTestListBinding;

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
public class TestListActivity extends BaseActivity<ActivityTestListBinding> {
    private TestListViewModel viewModel;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_test_list;
    }

    @Override
    public void initView() {
        super.initView();
        viewModel = createViewModel(TestListViewModel.class);
        //绑定适配器
        binding.setVariable(BR.adapter, new TestListAdapter());
    }

    @Override
    public void initEvent() {
        super.initEvent();
        viewModel.getDataList().observe(this, list -> {
            binding.getAdapter().setList(list);
        });

        //临时写
        binding.btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getListData();
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getListData();
    }

}
