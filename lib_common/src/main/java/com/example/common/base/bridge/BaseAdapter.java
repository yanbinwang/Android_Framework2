package com.example.common.base.bridge;

import androidx.databinding.BaseObservable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/11.
 * 基础适配器，基础列表传入数据模型和xml布局即可，特殊的需要重写父类方法
 */
public abstract class BaseAdapter<T extends BaseObservable> extends RecyclerView.Adapter<BaseViewHolder> {
    protected List<T> list;

    public BaseAdapter() {
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setList(List list) {
        this.list = list;
        notifyDataSetChanged();
    }

}
