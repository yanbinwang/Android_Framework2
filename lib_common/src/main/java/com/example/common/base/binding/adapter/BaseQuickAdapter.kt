package com.example.common.base.binding.adapter

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
    protected var mBinding: VDB? = null

    constructor() : super()

    constructor(bean: T?) : super(bean)

    constructor(list: ArrayList<T>?) : super(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        val aClass = try {
            (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(1) as? Class<*>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //避免在适配器中持有 Context 引用，通过 holder.itemView.context 获取上下文
        return onCreateViewBindingHolder(parent, aClass as? Class<VDB>)
    }

    override fun onConvert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>?) {
        mBinding = holder.viewBinding()
    }

    override fun onViewRecycled(holder: BaseViewDataBindingHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}