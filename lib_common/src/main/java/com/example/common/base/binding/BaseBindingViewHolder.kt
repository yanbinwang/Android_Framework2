package com.example.common.base.binding

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/6/5.
 * 基础适配器绑定
 */
class BaseBindingViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root.rootView) {

    fun <VDB : ViewDataBinding?> getBinding(): VDB {
        return binding as VDB
    }

}