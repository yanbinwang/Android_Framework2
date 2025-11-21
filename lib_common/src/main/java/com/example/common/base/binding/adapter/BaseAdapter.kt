package com.example.common.base.binding.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.common.base.binding.adapter.BaseItemType.BEAN
import com.example.common.base.binding.adapter.BaseItemType.LIST
import com.example.common.base.bridge.BaseViewModel
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
    protected var onItemClick: ((v: View?, t: T?, position: Int) -> Unit)? = null
    protected var onItemLongClick: ((v: View?, t: T?, position: Int) -> Boolean)? = null

    /**
     * 默认是返回集合
     */
    constructor() {
        data = ArrayList()
    }

    /**
     * 传入对象的方法
     */
    constructor(bean: T?) {
        itemType = BEAN
        bean ?: return
        t = bean
    }

    /**
     * 传入集合的方法
     */
    constructor(list: ArrayList<T>?) {
        itemType = LIST
        list ?: return
        data = list
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
        // 扁平化 payloads，处理系统多套的一层 List
        val flatPayloads = mutableListOf<Any>()
        payloads?.forEach { item ->
            // 如果 item 是 List，就把里面的元素拆出来（扁平化）；否则直接添加
            if (item is List<*>) {
                // 过滤非 Any 类型，避免强转错误
                flatPayloads.addAll(item.filterIsInstance<Any>())
            } else {
                flatPayloads.add(item)
            }
        }
        val position = holder.absoluteAdapterPosition
        // 注意判断当前适配器是否具有头部view
        holder.itemView.click {
            onItemClick?.invoke(it, data.safeGet(position), position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(it, data.safeGet(position), position).orFalse
        }
        onConvert(holder, getItem(position), flatPayloads)
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
     * 适用于 “需要立即更新视图的场景（如 RecyclerView 复用）”，避免视图显示旧数据
     * setVariable原理:
     * 异步绑定：
     * 当调用 binding.setVariable(BR.bean, item) 或 binding.bean = item 时，DataBinding 并不会立即执行所有的绑定表达式（比如 android:text="@{bean.amount}"）。为了优化性能，它会将这些绑定操作推迟到下一帧（Frame） 绘制之前执行。这个过程是异步的。
     * RecyclerView 的复用：RecyclerView 在滚动或刷新时，会快速地复用 ViewHolder。在 onBindViewHolder 中，你为复用的 ViewHolder 设置了新的 bean，但由于绑定是异步的，ViewHolder 上的视图可能不会立即更新。
     * 闪烁的根源：
     * 在 onBindViewHolder 执行完毕后，RecyclerView 会立即将这个 ViewHolder 显示在屏幕上。如果此时 DataBinding 的异步绑定还未完成，那么用户看到的将是这个 ViewHolder 上一次复用时残留的旧数据。直到下一帧绘制时，新数据才会显示出来，这就造成了 “闪烁” 或 “数据跳变” 的现象。
     * executePendingBindings() 的作用就是：强制 DataBinding 立即执行所有等待中的绑定表达式，而不是等到下一帧。
     * 风险点:
     * 1) 绑定表达式中包含 “耗时操作”（比如复杂计算、同步 IO）
     * 风险：如果布局 xml 中绑定表达式写了耗时逻辑（比如 @{bean.calculateComplexData()}），executePendingBindings() 会在主线程同步执行这些逻辑，可能导致列表滑动卡顿（因为 onBindViewHolder 是主线程执行的）。
     * 规避方式：
     * 永远不要在绑定表达式中写复杂计算、IO 操作（这是 DataBinding 的基础规范，和 executePendingBindings() 无关）；
     * 复杂逻辑提前在 ViewModel/Repository 中预处理（比如 bean 直接持有计算后的结果，而非绑定时分计算）。
     * 2) 绑定表达式中包含 “异步回调依赖”（极罕见）
     * 风险：如果绑定表达式依赖某个异步结果（比如 @{bean.asyncData}，而 asyncData 是在绑定后才通过回调赋值的），executePendingBindings() 会因为 “数据未就绪” 而绑定空值，但这种情况和 executePendingBindings() 无关 —— 即使不调用，下一帧绑定也会是空值，本质是 “数据准备时机错误”。
     * 规避方式：确保设置 binding.bean = item 时，item 中的所有绑定字段都已就绪（异步数据提前加载完成）。
     */
    protected fun setExecutePendingVariable(mBinding: ViewDataBinding?, variableId: Int, value: Any?) {
        value ?: return
        mBinding?.let {
            it.setVariable(variableId, value)
            it.executePendingBindings()
        }
    }

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
        changed(findIndex(func), bean)
    }

    /**
     * 传入要改变的对象和对象下标，直接刷新对应item
     */
    fun changed(index: Int?, bean: T?) {
        if (index == null || bean == null) return
        if (index in data.indices) {
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
        if (index in data.indices) {
            data.safeSet(index, bean)
            notifyItemChanged(index, payloads)
        }
    }

    /**
     * 删除符合条件的对象
     */
    fun removed(func: ((T) -> Boolean)) {
        removed(findIndex(func))
    }

    /**
     * 删除指定下标对象
     */
    fun removed(index: Int?) {
        if (index == null) return
        if (index in data.indices) {
            data.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 查找并返回符合条件的对象
     */
    fun findItem(func: ((T) -> Boolean)): T? {
        val index = data.findIndexOf(func)
        return data.safeGet(index)
    }

    /**
     * 查找到符合条件的对象，返回下标和对象本身，调用notifyItemChanged（position）修改改变的值
     */
    fun findItemOf(func: ((T) -> Boolean)): Pair<Int, T?> {
        val index = data.findIndexOf(func)
        return index to data.safeGet(index)
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
    fun findIndex(func: ((T) -> Boolean)): Int {
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
    fun setOnItemClickListener(listener: ((v: View?, t: T?, position: Int) -> Unit)) {
        this.onItemClick = listener
    }

    fun setOnItemLongClickListener(listener: ((v: View?, t: T?, position: Int) -> Boolean)) {
        this.onItemLongClick = listener
    }

}