package com.example.common.base.binding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.base.utils.function.findIndexOf

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础适配器，适用于定制页面，加头加尾，需要重写onCreateViewHolder
 */
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewDataBindingHolder?> {
    /**
     * 适配器类型-后续可扩展
     */
    private var itemType = BaseItemType.MODEL
    /**
     * 数据类型为集合
     */
    var data: MutableList<T> = ArrayList()
        set(value) {
            val size = data.size
            //设置集合类型不相同时替换
            if (value !== field) {
                if (!value.isNullOrEmpty()) field.addAll(value)
            } else {
                field.clear()
                if (!value.isNullOrEmpty()) field.addAll(ArrayList(value))
            }
            notifyDataSetChanged()
        }
    /**
     * 数据类型为对象
     */
    var t: T? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemClick: ((position: Int) -> Unit)? = null

    /**
     * 默认是返回对象
     */
    constructor()

    /**
     * 传入对象的方法
     */
    constructor(model: T?) {
        if (t != null) {
            t = model
            itemType = BaseItemType.MODEL
        }
    }

    /**
     * 传入集合的方法
     */
    constructor(list: MutableList<T>?) {
        if (list != null) {
            data = list
            itemType = BaseItemType.LIST
        }
    }

    override fun getItemCount(): Int {
        return when (itemType) {
            BaseItemType.LIST -> data.size
            BaseItemType.MODEL -> 0
        }
    }

    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int) {
        onConvert(holder, position)
    }

    /**
     * 局部刷新notifyItemChanged可传入一个集合，用来判断是否刷新item里的某一个view，此时可以使用payloads集合
     */
    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int, payloads: MutableList<Any>) {
        onConvert(holder, position, payloads)
    }

    private fun onConvert(holder: BaseViewDataBindingHolder, position: Int, payloads: MutableList<Any>? = null) {
        //注意判断当前适配器是否具有头部view
        holder.itemView.setOnClickListener { onItemClick?.invoke(holder.absoluteAdapterPosition) }
        convert(holder, when (itemType) {
                BaseItemType.LIST -> data[position]
                BaseItemType.MODEL -> t
            }, payloads)
    }


    /**
     * 构建ViewBinding
     */
    protected fun <VB : ViewDataBinding> onCreateViewBindingHolder(parent: ViewGroup, aClass: Class<VB>): BaseViewDataBindingHolder {
        var binding: VB? = null
        try {
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            binding = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        } catch (ignored: Exception) {
        } finally {
            return BaseViewDataBindingHolder(binding!!)
        }
    }

    /**
     * 统一回调
     */
    protected abstract fun convert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>? = null)

    /**
     * 如果类型是集合，可以调取该方法实现局部item刷新
     */
    fun notifyItemChanged(func: ((T) -> Boolean)) {
        val index = data.findIndexOf(func)
        if (index != -1) notifyItemChanged(index)
    }

    fun notifyItemChanged(func: ((T) -> Boolean), payloads: MutableList<Any>) {
        val index = data.findIndexOf(func)
        if (index != -1) notifyItemChanged(index, payloads)
    }

    /**
     * 删除某个条目
     */
    fun notifyItemRemoved(func: ((T) -> Boolean)) {
        val index = data.findIndexOf(func)
        if (index != -1) notifyItemRemoved(index)
    }

}