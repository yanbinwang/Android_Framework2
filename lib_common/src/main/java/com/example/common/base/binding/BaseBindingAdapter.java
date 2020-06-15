package com.example.common.base.binding;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by WangYanBin on 2020/6/11.
 */
public abstract class BaseBindingAdapter<T extends BaseObservable> extends RecyclerView.Adapter<BaseBindingAdapter.BaseBindingViewHolder>{
    private List<T> list;

    public BaseBindingAdapter(List<T> list) {
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public static class BaseBindingViewHolder extends RecyclerView.ViewHolder{
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
