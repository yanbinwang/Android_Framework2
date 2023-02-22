package com.example.common.base.binding.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.common.base.binding.adapter.BaseItemType.BEAN
import com.example.common.base.binding.adapter.BaseItemType.LIST
import com.example.framework.utils.function.value.*
import com.example.framework.utils.function.view.click

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础适配器，适用于定制页面，加头加尾，需要重写onCreateViewHolder
 */
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewDataBindingHolder?> {
    /**
     * 适配器类型-后续可扩展
     */
    private var itemType = BEAN

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
            itemType = BEAN
        }
    }

    /**
     * 传入集合的方法
     */
    constructor(list: MutableList<T>?) {
        if (list != null) {
            data = list
            itemType = LIST
        }
    }

    override fun getItemCount(): Int {
        return when (itemType) {
            LIST -> data.size
            BEAN -> 1
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
            LIST -> data.safeGet(position)
            BEAN -> t
        }, payloads)
    }

    /**
     * 构建ViewBinding
     */
    protected fun <VDB : ViewDataBinding> onCreateViewBindingHolder(parent: ViewGroup, aClass: Class<VDB>): BaseViewDataBindingHolder {
        lateinit var binding: VDB
        try {
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            binding = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as VDB
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
    fun changed(func: ((T) -> Boolean)) {
        data.findIndexOf(func).apply { if (this != -1) notifyItemChanged(this) }
    }

    /**
     * 查找到符合条件的对象，改变为新的对象并刷新对应item
     */
    fun changed(func: ((T) -> Boolean), bean: T) {
        changed(data.findIndexOf(func), bean)
    }

    /**
     * 传入要改变的对象和对象下标，直接刷新对应item
     */
    fun changed(index: Int, bean: T) {
        if (index != -1) {
            data[index] = bean
            notifyItemChanged(index)
        }
    }

    fun changed(func: ((T) -> Boolean), payloads: MutableList<Any>, bean: T) {
        changed(data.findIndexOf(func), payloads, bean)
    }

    fun changed(index: Int, payloads: MutableList<Any>, bean: T) {
        if (index != -1) {
            data[index] = bean
            notifyItemChanged(index, payloads)
        }
    }

    /**
     * 删除某个条目
     */
    fun removed(func: ((T) -> Boolean)) {
        removed(data.findIndexOf(func))
    }

    fun removed(index: Int) {
        if (index != -1) {
            data.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 刷新/添加数据
     * page：手机端整理的分页对象类，和服务端约定好data内嵌套的格式
     * hasRefresh：指定此次的数据是否是刷新的数据（由外层刷新控件或手动指定）
     * onConvert：返回此次请求获取到的集合
     * onEmpty：当前适配器的集合为空时才会回调
     */
//    fun notify(page: Page<T>?, hasRefresh: Boolean? = true, onConvert: (list: List<T>) -> Unit = {}, onEmpty: () -> Unit = {}) {
//        val list = page?.list.orEmpty()
//        if (hasRefresh.orFalse) refresh(list) else insert(list)
//        onConvert.invoke(list)
//        if (data.safeSize == 0) onEmpty.invoke()
//    }

    fun notify(list: List<T>, hasRefresh: Boolean = true, onEmpty: () -> Unit = {}) {
        if (hasRefresh) refresh(list) else insert(list)
        if (data.safeSize == 0) onEmpty.invoke()
    }

    /**
     * 查找并返回符合条件的对象
     */
    fun find(func: ((T) -> Boolean)): T? {
        val index = data.findIndexOf(func)
        return if (index != -1) data.safeGet(index) else null
    }

    /**
     * 查找到符合条件的对象，返回下标和对象本身，调用notifyItemChanged（position）修改改变的值
     */
    fun find(func: ((T) -> Boolean), onConvert: (position: Int, bean: T?) -> Unit) {
        data.findIndexOf(func).apply { onConvert.invoke(this, data.safeGet(this)) }
    }

    /**
     * 查找符合条件的data数据总数
     */
    fun findCount(func: ((T) -> Boolean)): Int {
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