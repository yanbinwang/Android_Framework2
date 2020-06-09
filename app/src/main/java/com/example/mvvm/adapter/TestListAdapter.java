package com.example.mvvm.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.base.bridge.BaseViewHolder;
import com.example.mvvm.R;
import com.example.mvvm.databinding.ItemTestBinding;
import com.example.mvvm.model.TestListModel;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/5.
 */
public class TestListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<TestListModel> list;

    public TestListAdapter(List<TestListModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_test, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        //建立对象和item的绑定关系,基本逻辑皆可在xml中实现,复杂的逻辑可在此处实现
        ItemTestBinding binding = holder.getBinding();
        binding.setModel(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
