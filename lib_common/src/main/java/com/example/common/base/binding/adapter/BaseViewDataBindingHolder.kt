package com.example.common.base.binding.adapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础复用的ViewHolder，传入对应的ViewBinding拿取布局Binding
 */
@Suppress("UNCHECKED_CAST")
class BaseViewDataBindingHolder(parent: ViewGroup, private val binding: ViewDataBinding?) : RecyclerView.ViewHolder(binding?.root ?: View(parent.context)) {

    fun <VDB : ViewDataBinding> getBinding(): VDB? {
        return binding as? VDB
    }

}