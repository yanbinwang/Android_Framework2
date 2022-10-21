package com.example.common.base.binding

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/17.
 * 快捷适配器，传入对应的ViewBinding即可
 */
abstract class BaseQuickAdapter<T, VB : ViewDataBinding> : BaseAdapter<T> {
    protected var context: Context? = null

    constructor() : super(ArrayList())

    constructor(model: T?) : super(model)

    constructor(list: MutableList<T>?) : super(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        context = parent.context
        val superclass = javaClass.genericSuperclass
        val aClass = (superclass as ParameterizedType).actualTypeArguments[1] as Class<*>
        return onCreateViewBindingHolder(parent, aClass as Class<VB>)
    }

}