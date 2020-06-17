package com.example.mvvm.adapter;

import androidx.databinding.DataBindingUtil;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.example.mvvm.R;
import com.example.mvvm.databinding.ItemTestBinding;
import com.example.mvvm.model.TestListModel;

import org.jetbrains.annotations.NotNull;

/**
 * Created by WangYanBin on 2020/6/5.
 */
public class TestListAdapter extends BaseQuickAdapter<TestListModel, BaseDataBindingHolder> {

    public TestListAdapter() {
        super(R.layout.item_test);
    }

    @Override
    protected void onItemViewHolderCreated(@NotNull BaseDataBindingHolder viewHolder, int viewType) {
        super.onItemViewHolderCreated(viewHolder, viewType);
        DataBindingUtil.bind(viewHolder.itemView);
    }

    @Override
    protected void convert(@NotNull BaseDataBindingHolder helper, TestListModel item) {
        if (item == null) {
            return;
        }
        //设置数据
        ItemTestBinding binding = (ItemTestBinding) helper.getDataBinding();
        if (binding != null) {
            binding.setModel(item);
        }
    }
}