package com.example.mvvm.activity;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.R;
import com.example.mvvm.adapter.TestListAdapter;
import com.example.mvvm.bridge.TestListViewModel;
import com.example.mvvm.databinding.ActivityTestListBinding;
import com.example.mvvm.model.TestListModel;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
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
    public void initEvent() {
        super.initEvent();
        viewModel.dataList.observe(this, new Observer<List<TestListModel>>() {
            @Override
            public void onChanged(List<TestListModel> testListModels) {
                //绑定适配器
                binding.setAdapter(new TestListAdapter(testListModels));
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getListData();
    }

}
