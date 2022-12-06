package com.example.common.base.binding.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.base.utils.function.value.findIndexOf
import com.example.base.utils.function.value.orZero
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.function.value.safeSize
import com.example.base.utils.function.view.click
import com.example.common.base.page.Page
import com.example.common.base.page.Paging

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
    private var data: MutableList<T> = ArrayList()
    /**
     * 数据类型为对象
     */
    private var t: T? = null
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
        convert(holder, when (itemType) {
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
     * 刷新符合条件的item（数据在item内部更改）
     */
    fun itemChanged(func: ((T) -> Boolean)) {
        data.findIndexOf(func).apply { if (this != -1) notifyItemChanged(this) }
    }

    /**
     * 查找到符合条件的对象，改变为新的对象并刷新对应item
     */
    fun itemChanged(func: ((T) -> Boolean), bean: T) {
        itemChanged(data.findIndexOf(func), bean)
    }

    /**
     * 传入要改变的对象和对象下标，直接刷新对应item
     */
    fun itemChanged(index: Int, bean: T) {
        if (index != -1) {
            data[index] = bean
            notifyItemChanged(index)
        }
    }

    fun itemChanged(func: ((T) -> Boolean), payloads: MutableList<Any>, bean: T) {
        itemChanged(data.findIndexOf(func), payloads, bean)
    }

    fun itemChanged(index: Int, payloads: MutableList<Any>, bean: T) {
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
     * 刷新数据
     */
    fun itemNotify(it: Page<T>?, paging: Paging, onConvert: (newList: MutableList<T>) -> Unit = {}, onEmpty: () -> Unit = {}) {
        paging.totalCount = it?.total.orZero
        val newList = it?.list ?: ArrayList()
        if (paging.hasRefresh) refresh(newList) else insert(newList)
        onConvert.invoke(newList)
        if (data.safeSize == 0) onEmpty.invoke()
        paging.currentCount = data.size
    }

    fun itemNotify(it: Page<T>?, onConvert: (list: MutableList<T>) -> Unit = {}, onEmpty: () -> Unit = {}) {
        val newList = it?.list ?: ArrayList()
        refresh(newList)
        onConvert.invoke(newList)
        if (data.safeSize == 0) onEmpty.invoke()
    }

    /**
     * 查找并返回符合条件的对象
     */
    fun itemFind(func: ((T) -> Boolean)): T? {
        val index = data.findIndexOf(func)
        return if (index != -1) data.safeGet(index) else null
    }

    /**
     * 查找到符合条件的对象，返回下标和对象本身，调用notifyItemChanged（position）修改改变的值
     */
    fun itemFind(func: ((T) -> Boolean), onConvert: (position: Int, bean: T?) -> Unit) {
        data.findIndexOf(func).apply { onConvert.invoke(this, data.safeGet(this)) }
    }

    /**
     * 查找符合条件的data数据总数
     */
    fun itemFindCount(func: ((T) -> Boolean)): Int {
        return data.filter(func).size
    }

    /**
     * 根据下标获取对象
     */
    fun item(position: Int): T? {
        return data.safeGet(position)
    }

    /**
     * 获取当前集合
     */
    fun list(): MutableList<T> {
        return data
    }

    /**
     * 获取当前集合长度
     */
    fun size(): Int {
        return data.safeSize
    }

    /**
     * 获取当前对象
     */
    fun bean(): T? {
        return t
    }

    /**
     * 刷新集合
     */
    fun refresh(list: List<T>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 刷新对象
     */
    fun refresh(bean: T) {
        t = bean
        notifyDataSetChanged()
    }

    /**
     * 插入集合
     */
    fun insert(list: List<T>) {
        val size = data.size
        data.addAll(list)
        notifyItemRangeInserted(size, list.size)
    }

    /**
     * 对应下标插入对象
     */
    fun insert(position: Int, item: T) {
        if (position !in data.indices) return
        data.add(position, item)
        notifyItemInserted(position)
    }

    /**
     * 集合末尾揣入对象
     */
    fun insert(item: T) {
        data.add(item)
        notifyItemInserted(data.size - 1)
    }

}