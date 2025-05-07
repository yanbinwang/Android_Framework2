package com.example.common.base.binding.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.example.common.base.binding.adapter.BaseItemType.BEAN
import com.example.common.base.binding.adapter.BaseItemType.LIST
import com.example.common.base.bridge.BaseViewModel
import com.example.framework.utils.function.value.findAndRemove
import com.example.framework.utils.function.value.findIndexOf
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础适配器，适用于定制页面，加头加尾，需要重写onCreateViewHolder
 */
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewDataBindingHolder> {
    /**
     * 适配器类型-后续可扩展
     */
    private var itemType: BaseItemType = LIST // 默认是 LIST 类型
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
    private var onItemClick: ((t: T?, position: Int) -> Unit)? = null

    /**
     * 默认是返回集合
     */
    constructor() {
        data = ArrayList() // 显式初始化空列表
    }

    /**
     * 传入对象的方法
     */
    constructor(bean: T?) {
        itemType = BEAN
        t = bean
    }

    /**
     * 传入集合的方法
     */
    constructor(list: ArrayList<T>?) {
        itemType = LIST
        data = list.orEmpty().toMutableList()
    }

    override fun getItemCount(): Int {
        return when (itemType) {
            LIST -> data.safeSize
            BEAN -> if (t != null) 1 else 0
        }
    }

    /**
     * 基础重载方法，当 RecyclerView 需要完全重新绑定一个 ViewHolder 时会调用此方法。常见的触发场景包括：
     * 该方法用于完全重新绑定 ViewHolder，意味着它会更新 ViewHolder 中的所有视图元素，以反映数据集中对应位置的数据
     * 1）首次创建 ViewHolder 并进行绑定时。
     * 2）调用 notifyDataSetChanged() 方法，它会通知 RecyclerView 整个数据集可能已改变，此时所有可见的 ViewHolder 都会调用此方法进行重新绑定。
     * 3）调用 notifyItemChanged(position) 但没有传递 payload 时，RecyclerView 会认为需要完全更新该位置的 ViewHolder
     */
    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int) {
        onConvertHolder(holder, null)
    }

    /**
     * 这个重载方法在调用 notifyItemChanged(position, payload) 时被触发，
     * 其中 payload 是一个包含变化信息的对象。它允许你只更新 ViewHolder 中部分视图，而不是整个视图。这种方式更高效，因为它避免了不必要的视图更新
     * if (payloads.isEmpty()) {
     *         // 如果 payloads 为空，说明没有部分更新信息，调用全量更新方法
     *         onBindViewHolder(holder, position)
     *     } else {
     *         // 根据 payloads 中的信息进行局部更新
     *         for (payload in payloads) {
     *             if (payload is UpdateTitlePayload) {
     *                 holder.titleTextView.text = getItem(position).title
     *             } else if (payload is UpdateDescriptionPayload) {
     *                 holder.descriptionTextView.text = getItem(position).description
     *             }
     *         }
     *     }
     */
    override fun onBindViewHolder(holder: BaseViewDataBindingHolder, position: Int, payloads: MutableList<Any>) {
        onConvertHolder(holder, payloads)
    }

    /**
     * 如果payloads不为空，回调里做局部刷新
     */
    private fun onConvertHolder(holder: BaseViewDataBindingHolder, payloads: MutableList<Any>?) {
        val position = holder.absoluteAdapterPosition
        //注意判断当前适配器是否具有头部view
        holder.itemView.click {
            onItemClick?.invoke(data.safeGet(position), position)
        }
        onConvert(holder, getItem(position), payloads)
    }

    /**
     * 获取对应下标的class
     */
    private fun getItem(position: Int): T? {
        return when (itemType) {
            LIST -> if (position in data.indices) data.safeGet(position) else null
            BEAN -> t
        }
    }

    /**
     * 统一回调
     */
    protected abstract fun onConvert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>?)

    /**
     * 刷新符合条件的item（数据在item内部更改）
     */
    fun changed(func: ((T) -> Boolean)) {
        data.findIndexOf(func).apply {
            if (this != -1) notifyItemChanged(this)
        }
    }

    /**
     * 查找到符合条件的对象，改变为新的对象并刷新对应item
     */
    fun changed(func: ((T) -> Boolean), bean: T?) {
        changed(findIndexOf(func), bean)
    }

    /**
     * 传入要改变的对象和对象下标，直接刷新对应item
     */
    fun changed(index: Int?, bean: T?) {
        if (index == null || bean == null) return
        if (index != -1 && data.safeGet(index) != null) {
            data.safeSet(index, bean)
            notifyItemChanged(index)
        }
    }

    /**
     * 传入符合条件的下标，给到需要刷新的payloads，以及对应的新data class
     */
    fun changed(func: ((T) -> Boolean), payloads: MutableList<Any>, bean: T?) {
        changed(data.findIndexOf(func), payloads, bean)
    }

    fun changed(index: Int?, payloads: MutableList<Any>, bean: T?) {
        if (index == null || bean == null) return
        if (index != -1 && data.safeGet(index) != null) {
            data.safeSet(index, bean)
            notifyItemChanged(index, payloads)
        }
    }

    /**
     * 删除符合条件的对象
     */
    fun removed(func: ((T) -> Boolean)) {
        data.findAndRemove(func)
    }

    /**
     * 删除指定下标对象
     */
    fun removed(index: Int?) {
        if (index == null) return
        if (index != -1 && data.safeGet(index) != null) {
            data.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 查找并返回符合条件的对象
     */
    fun find(func: ((T) -> Boolean)): T? {
        val index = data.findIndexOf(func)
        return data.safeGet(index)
    }

    /**
     * 查找到符合条件的对象，返回下标和对象本身，调用notifyItemChanged（position）修改改变的值
     */
    fun find(func: ((T) -> Boolean), listener: (Pair<Int, T?>) -> Unit) {
        data.findIndexOf(func).apply {
            listener.invoke(this to data.safeGet(this))
        }
    }

    /**
     * 查找符合条件的data数据总数
     */
    fun findCount(func: ((T) -> Boolean)): Int {
        return data.filter(func).safeSize
    }

    /**
     * 返回查找到的符合条件的下标
     */
    fun findIndexOf(func: ((T) -> Boolean)): Int {
        return data.findIndexOf(func)
    }

    /**
     * 根据下标获取对象
     */
    fun item(position: Int?): T? {
        if (position == null) return null
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
        return if (itemType == LIST) data.safeSize else 1
    }

    /**
     * 获取当前对象
     */
    fun bean(): T? {
        return t
    }

    /**
     * 刷新/添加数据
     * list：此次需要修改的集合
     * hasRefresh：指定此次的数据是否是刷新的数据（由外层刷新控件或手动指定）
     * onEmpty：当前适配器的集合为空时才会回调
     */
    fun notify(list: List<T>?, hasRefresh: Boolean? = true, onEmpty: () -> Unit = {}) {
        list ?: return
        if (hasRefresh.orFalse) refresh(list) else insert(list)
        if (size() == 0) onEmpty.invoke()
    }

    fun <VM : BaseViewModel> notify(list: List<T>?, viewModel: VM?, resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        viewModel?.apply {
            notify(list, hasRefresh()) { empty(resId, text, refreshText, width, height) }
            setCurrentCount(size())
        }
    }

    /**
     * 刷新集合
     */
    fun refresh(list: List<T>?) {
        if (null == list) return
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 刷新对象
     */
    fun refresh(bean: T?) {
        if (null == bean) return
        t = bean
        notifyDataSetChanged()
    }

    /**
     * 插入集合
     */
    fun insert(list: List<T>?) {
        if (null == list) return
        val positionStart = size()
        data.addAll(list)
        notifyItemRangeInserted(positionStart, list.safeSize)
    }

    /**
     * 对应下标插入集合
     */
    fun insert(position: Int?, list: List<T>?) {
        if (null == position || null == list) return
        data.addAll(position, list)
        notifyDataSetChanged()
    }

    /**
     * 对应下标插入对象
     */
    fun insert(position: Int?, item: T?) {
        if (null == position || null == item) return
        if (position !in data.indices) return
        data.add(position, item)
        notifyItemInserted(position)
    }

    /**
     * 集合末尾揣入对象
     */
    fun insert(item: T?) {
        if (null == item) return
        data.add(item)
        notifyItemInserted(data.safeSize - 1)
    }

    /**
     * 更新集合->假设两组集合长度相同，固定socket推送
     * 重写T的equals和hashCode
     */
    fun update(list: List<T>?) {
        if (null == list) return
        val diffIndices = mutableListOf<Int>()
        // 遍历新集合和旧集合，找出不相等元素的下标
        for (i in list.indices) {
            if (i < data.safeSize && list.safeGet(i) != data.safeGet(i)) {
                diffIndices.add(i)
            }
        }
        // 更新数据
        data.clear()
        data.addAll(list)
        // 刷新不同的项
        for (index in diffIndices) {
            // 这里直接使用 index 来更新对应位置的项
            notifyItemChanged(index)
        }
    }

    /**
     * 适配器点击
     */
    fun setOnItemClickListener(onItemClick: ((t: T?, position: Int) -> Unit)) {
        this.onItemClick = onItemClick
    }

}