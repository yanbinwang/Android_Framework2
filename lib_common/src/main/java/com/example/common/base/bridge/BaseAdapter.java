package com.example.common.base.bridge;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/11.
 * 基础适配器，基础列表传入数据模型和xml布局即可，特殊的需要重写父类方法
 */
public abstract class BaseAdapter<T extends BaseObservable> extends RecyclerView.Adapter<BaseViewHolder> {
    protected List<T> list;
    private int layoutId;

    public BaseAdapter() {
    }

    public BaseAdapter(int layoutId) {
        this.layoutId = layoutId;
    }

    public BaseAdapter(List<T> list, int layoutId) {
        this.list = list;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return 0 != layoutId ? new BaseViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false)) : null;
    }

//    @Override
//    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
//        holder.getBinding().setVariable(BR._all, list.get(position));
//    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setList(List list) {
        this.list = list;
        notifyDataSetChanged();
    }

}
