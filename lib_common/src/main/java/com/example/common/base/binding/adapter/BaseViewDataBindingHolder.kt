package com.example.common.base.binding.adapter

import android.view.LayoutInflater
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

    companion object {
        /**
         * 构建ViewBinding
         */
        @JvmStatic
        fun <VDB : ViewDataBinding> onCreateViewBindingHolder(parent: ViewGroup, aClass: Class<VDB>?): BaseViewDataBindingHolder {
            var binding: VDB? = null
            try {
                val method = aClass?.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
                binding = method?.invoke(null, LayoutInflater.from(parent.context), parent, false) as? VDB
            } catch (_: Exception) {
            }
            return BaseViewDataBindingHolder(parent, binding)
        }
    }

    fun <VDB : ViewDataBinding> getBinding(): VDB? {
        return binding as? VDB
    }

}