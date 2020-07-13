package com.example.mvvm.activity;

import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.common.base.BaseActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.BR;
import com.example.mvvm.R;
import com.example.mvvm.adapter.TestListAdapter;
import com.example.mvvm.bridge.TestListViewModel;
import com.example.mvvm.databinding.ActivityTestListBinding;

import org.jetbrains.annotations.Nullable;

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

        binding.getAdapter().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                showToast("整体点击：" + position);
            }
        });

        binding.getAdapter().setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                switch (view.getId()){
                    case R.id.iv_img:
                        showToast("图片点击：" + position);
                        break;
                    case R.id.tv_title:
                        showToast("标题点击：" + position);
                        break;
                }
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getListData();
    }

}
