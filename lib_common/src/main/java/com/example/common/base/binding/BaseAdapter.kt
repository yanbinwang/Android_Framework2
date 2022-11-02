package com.example.common.base.binding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.base.utils.function.value.findIndexOf
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.function.view.click

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础适配器，适用于定制页面，加头加尾，需要重写onCreateViewHolder
 */
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewDataBindingHolder?> {
    /**
     * 适配器类型-后续可扩展
     */
    private var itemType = BaseItemType.BEAN
    /**
     * 数据类型为集合
     */
    var data: MutableList<T> = ArrayList()
        set(value) {
            //设置集合类型不相同时替换
            if (value.isNotEmpty()) {
                if (value !== field) {
                    field.addAll(value)
                } else {
                    field.clear()
                    field.addAll(ArrayList(value))
                }
                notifyDataSetChanged()
            }
        }
    /**
     * 数据类型为对象
     */
    var t: T? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    /**
     * 点击回调，返回对象和下标
     */
    var onItemClick: ((t: T?, position: Int) -> Unit)? = null

    /**
     * 默认是返回对象
     */
    constructor()

    /**
     * 传入对象的方法
     */
    constructor(bean: T?) {
        if (t != null) {
            t = bean
            itemType = BaseItemType.BEAN
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
            BaseItemType.BEAN -> 1
        }
    }

    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int) {
        onConvertHolder(holder)
    }

    /**
     * 局部刷新notifyItemChanged可传入一个集合，用来判断是否刷新item里的某一个view，此时可以使用payloads集合
     */
    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int, payloads: MutableList<Any>) {
        onConvertHolder(holder, payloads)
    }

    private fun onConvertHolder(holder: BaseViewDataBindingHolder, payloads: MutableList<Any>? = null) {
        val position = holder.absoluteAdapterPosition
        //注意判断当前适配器是否具有头部view
        holder.itemView.click { onItemClick?.invoke(data.safeGet(position), position) }
        convert(
            holder, when (itemType) {
                BaseItemType.LIST -> data.safeGet(position)
                BaseItemType.BEAN -> t
            }, payloads)
    }

    /**
     * 构建ViewBinding
     */
    protected fun <VB : ViewDataBinding> onCreateViewBindingHolder(parent: ViewGroup, aClass: Class<VB>): BaseViewDataBindingHolder {
        lateinit var binding: VB
        try {
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            binding = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        } catch (_: Exception) {
        }
        return BaseViewDataBindingHolder(binding)
    }

    /**
     * 统一回调
     */
    protected abstract fun convert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>? = null)

    /**
     * 如果类型是集合，可以调取该方法实现局部item刷新
     */
    fun itemChanged(func: ((T) -> Boolean)) {
        val index = data.findIndexOf(func)
        if (index != -1) notifyItemChanged(index)
    }

    /**
     * 查找到符合条件的对象，改变为新的对象并刷新对应item
     */
    fun itemChanged(func: ((T) -> Boolean), bean: T) {
        itemChanged(bean, data.findIndexOf(func))
    }

    /**
     * 传入要改变的对象和对象下标，直接刷新对应item
     */
    fun itemChanged(bean: T, index: Int) {
        if (index != -1) {
            data[index] = bean
            notifyItemChanged(index)
        }
    }

    fun itemChanged(func: ((T) -> Boolean), payloads: MutableList<Any>, bean: T) {
        itemChanged(bean, data.findIndexOf(func), payloads)
    }

    fun itemChanged(bean: T, index: Int, payloads: MutableList<Any>) {
        if (index != -1) {
            data[index] = bean
            notifyItemChanged(index, payloads)
        }
    }

    /**
     * 删除某个条目
     */
    fun itemRemoved(func: ((T) -> Boolean)) {
        itemRemoved(data.findIndexOf(func))
    }

    fun itemRemoved(index: Int) {
        if (index != -1) {
            data.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 根据条件，抓出当前适配器中符合条件的对象，返回一个Pair对象
     * a：下标 b：对象
     * 更新好后，调取notifyItemChanged（index）更新局部item
     */
    fun findData(func: ((T) -> Boolean)): Pair<Int, T?> {
        val index = data.findIndexOf(func)
        return if (index != -1) index to data[index] else -1 to null
    }

    /**
     * val bundle = getParcelable() as? AccessDetailBean
     * binding.adapter!!.apply {
     * val bean = findData { it.attestationId == bundle?.no }.apply { second?.fileLabel = bundle?.fileLabel }
     * notifyItemChanged(bean.first, bean.second)
     * }
     */
    fun findBean(func: ((T) -> Boolean)): T? {
        val index = data.findIndexOf(func)
        return if (index != -1) data[index] else null
    }

}