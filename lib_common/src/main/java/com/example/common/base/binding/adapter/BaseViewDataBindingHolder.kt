package com.example.common.base.binding.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础复用的ViewHolder，传入对应的ViewBinding拿取布局Binding
 */
open class BaseViewDataBindingHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun <VDB : ViewDataBinding> getBinding(): VDB {
        return binding as VDB
    }

}