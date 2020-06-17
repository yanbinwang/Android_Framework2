package com.example.common.base.bridge;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by WangYanBin on 2020/6/17.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;

    public BaseViewHolder(@NonNull ViewDataBinding binding) {
        super(binding.getRoot().getRootView());
        this.binding = binding;
    }

    public <VDB extends ViewDataBinding> VDB getBinding() {
        return (VDB) binding;
    }
}
