package com.example.common.base.binding

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础复用的ViewHolder，传入对应的ViewBinding拿取布局Binding
 */
open class BaseViewDataBindingHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun <VB : ViewDataBinding?> getBinding(): VB {
        return binding as VB
    }

}