package com.example.common.base.binding.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder.Companion.onCreateViewBindingHolder
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/17.
 * 快捷适配器，传入对应的ViewBinding即可
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseQuickAdapter<T, VDB : ViewDataBinding> : BaseAdapter<T> {
    protected var mContext: Context? = null
    protected var mBinding: VDB? = null

    constructor() : super(arrayListOf())

    constructor(bean: T?) : super(bean)

    constructor(list: ArrayList<T>?) : super(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        mContext = parent.context
        val aClass = try {
            (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(1) as? Class<*>
        } catch (_: Exception) {
        }
        return onCreateViewBindingHolder(parent, aClass as? Class<VDB>)
    }

    override fun onConvert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>?) {
        mBinding = holder.getBinding()
    }

}