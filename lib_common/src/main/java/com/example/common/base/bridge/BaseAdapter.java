package com.example.common.base.bridge;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/11.
 * 基础适配器，保留无参构造方法，用于适配传入对象
 */
public abstract class BaseAdapter<T extends BaseObservable> extends RecyclerView.Adapter<BaseAdapter.BaseBindingViewHolder> {
    protected List<T> list;

    public BaseAdapter() {
    }

    @Override
    public int getItemCount() {
        if (null == list) {
            return 0;
        }
        return list.size();
    }

    public void setList(List list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public static class BaseBindingViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BaseBindingViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot().getRootView());
            this.binding = binding;
        }

        public <VDB extends ViewDataBinding> VDB getBinding() {
            return (VDB) binding;
        }
    }

}
