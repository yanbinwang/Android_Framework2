package com.example.common.base.binding;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/11.
 */
public abstract class BaseBindingAdapter<T> extends RecyclerView.Adapter<BaseBindingViewHolder>{
    private List<T> list;

    public BaseBindingAdapter(List<T> list) {
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<T> list) {
        this.list = list;
    }

}
