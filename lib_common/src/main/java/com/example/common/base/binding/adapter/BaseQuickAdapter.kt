package com.example.common.base.binding.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/17.
 * 快捷适配器，传入对应的ViewBinding即可
 */
abstract class BaseQuickAdapter<T, VDB : ViewDataBinding> : BaseAdapter<T> {
    protected lateinit var context: Context
    protected lateinit var binding: VDB

    constructor() : super(ArrayList())

    constructor(bean: T?) : super(bean)

    constructor(list: MutableList<T>?) : super(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        context = parent.context
        val superclass = javaClass.genericSuperclass
        val aClass = (superclass as ParameterizedType).actualTypeArguments[1] as Class<*>
        return onCreateViewBindingHolder(parent, aClass as Class<VDB>)
    }

    override fun convert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>?) {
        binding = holder.getBinding()
    }

}