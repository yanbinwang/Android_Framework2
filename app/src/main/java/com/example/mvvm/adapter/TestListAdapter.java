package com.example.mvvm.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.common.base.bridge.BaseAdapter;
import com.example.mvvm.R;
import com.example.mvvm.databinding.ItemTestBinding;
import com.example.mvvm.model.TestListModel;

/**
 * Created by WangYanBin on 2020/6/5.
 */
public class TestListAdapter extends BaseAdapter<TestListModel> {

    @NonNull
    @Override
    public BaseAdapter.BaseBindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseBindingViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_test, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseAdapter.BaseBindingViewHolder holder, int position) {
        ItemTestBinding binding = holder.getBinding();
        binding.setModel(list.get(position));
    }

}