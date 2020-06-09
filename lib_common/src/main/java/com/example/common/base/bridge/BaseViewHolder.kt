package com.example.common.base.bridge

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/6/5.
 * 基础适配器绑定
 */
class BaseViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root.rootView) {

    fun <VDB : ViewDataBinding?> getBinding(): VDB {
        return binding as VDB
    }

}