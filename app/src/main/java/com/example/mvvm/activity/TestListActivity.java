package com.example.mvvm.activity;

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
public class TestListActivity extends BaseActivity<TestListViewModel, ActivityTestListBinding> {
//    private TestListAdapter adapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_test_list;
    }

//    @Override
//    public void initView() {
//        super.initView();
//        //绑定适配器
//        adapter = new TestListAdapter();
//        setVariable(BR.adapter, adapter);
//    }

    @Override
    public void initEvent() {
        super.initEvent();
//        viewModel.dataList.observe(this, list -> adapter.setList(list));
        viewModel.dataList.observe(this, list -> {
            setVariable(BR.adapter, new TestListAdapter(list));
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getListData();
    }

}
