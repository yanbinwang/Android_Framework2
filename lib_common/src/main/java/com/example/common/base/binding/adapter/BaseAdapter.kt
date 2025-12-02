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
import java.util.Collections
import kotlin.math.max

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础适配器，适用于定制页面，加头加尾，需要重写onCreateViewHolder
 */
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewDataBindingHolder> {
    /**
     * 适配器类型 -> 默认是 LIST 类型,后续可扩展
     */
    private var itemType: BaseItemType = LIST
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
     * 刷新/添加数据
     * list：此次需要修改的集合
     * hasRefresh：指定此次的数据是否是刷新的数据（由外层刷新控件或手动指定）
     * onEmpty：当前适配器的集合为空时才会回调
     */
    fun notify(list: List<T>?, hasRefresh: Boolean? = true, onEmpty: () -> Unit = {}) {
        if (null == list || list.isEmpty() || itemType != LIST) return
        if (hasRefresh.orFalse) {
            refresh(list)
        } else {
            insert(list)
        }
        if (size() == 0) {
            onEmpty.invoke()
        }
    }

    fun <VM : BaseViewModel> notify(list: List<T>?, viewModel: VM?, resId: Int? = null, resText: Int? = null, refreshText: Int? = null, width: Int? = null, height: Int? = null) {
        viewModel?.let {
            notify(list, it.hasRefresh()) {
                it.empty(resId, resText, refreshText, width, height)
            }
            it.setCurrentCount(size())
        }
    }

    /**
     * 更新固定集合
     * 1) 假设两组集合长度相同，固定socket推送
     * 2) 重写T的equals和hashCode
     * 3) kotlin的 zip 扩展函数,把两个集合「按索引一对一配对」，生成一个 “配对列表”，完全不关心元素内容是否相同，只看 “位置”
     * 集合 A 长度 > 集合 B 长度：只配对到集合 B 的最后一个元素，A 多出来的元素丢弃；
     * 集合 A 长度 < 集合 B 长度：只配对到集合 A 的最后一个元素，B 多出来的元素丢弃；
     * 结果是 List<Pair<T, U>>（两个元素的配对），而非筛选后的单集合。
     * val 姓名 = listOf("张三", "李四", "王五") // 长度3
     * val 分数 = listOf(90, 85)              // 长度2
     * // zip配对：按索引1对1，取短的长度（2）
     * val 姓名分数配对 = 姓名.zip(分数)
     * println(姓名分数配对)
     * // 输出：[(张三, 90), (李四, 85)]
     * // 解释：王五（索引2）没有分数配对，被丢弃；分数没有第三个元素，也不补
     */
    fun notify(list: List<T>?) {
        if (null == list || list.isEmpty() || itemType != LIST) return
        // 添加长度检查，防止意外情况
        if (list.safeSize != size()) {
            refresh(list)
            return
        }
        // 遍历新集合和旧集合，找出不相等元素的下标
        val diffPairs = list.zip(data)
            // 查询出发生改变的下标和对象 --> List<Pair<Int, T>>
            .mapIndexed { index, (newItem, oldItem) ->
                // 如果if条件不满足,返回null
                (index to newItem).takeIf { newItem != oldItem }
            }
            // 筛选掉null的值
            .filterNotNull()
        // 更新数据
        diffPairs.forEach { (index, item) ->
            changed(index, item)
        }
    }

    /**
     * 以服务器数据为基准，同步本地 RecyclerView 数据（更新+新增+删除）
     * @param list 服务器数据（基准数据）
     * @param idMatcher 唯一标识匹配规则：判断（本地元素，服务器元素）是否为同一元素（如按id匹配）
     * @param needUpdate 判断是否需要更新：同一标识下，（本地元素，服务器元素）是否有差异（有差异才更新）
     * // 数据类（无需重写 equals/hashCode）
     * data class User(val id: Int, val name: String, val amount: Double)
     * // 服务器数据（基准）
     * val serverUsers = listOf(
     *     User(1, "张三", 100.0),   // 本地无→新增
     *     User(2, "李四", 200.0),   // 本地无→新增
     *     User(3, "王五", 350.0)    // 本地有id=3，但amount变化→更新
     * )
     * // 本地数据（原有）
     * val localUsers = listOf(
     *     User(3, "王五", 300.0),   // id=3，amount和服务器不同→更新
     *     User(5, "赵六", 500.0),   // 服务器无id=5→删除
     *     User(7, "孙七", 700.0)    // 服务器无id=7→删除
     * )
     * // Adapter 同步调用（核心：传入两个自定义规则）
     * adapter.notify(
     *     serverList = serverUsers,
     *     // 规则1：按id匹配（唯一标识）
     *     idMatcher = { localItem, serverItem ->
     *         localItem.id == serverItem.id
     *     },
     *     // 规则2：name或amount变化则需要更新
     *     needUpdate = { localItem, serverItem ->
     *         localItem.name != serverItem.name || localItem.amount != serverItem.amount
     *     }
     * )
     */
    @Synchronized
    fun notify(list: List<T>?, idMatcher: ((localItem: T, serverItem: T) -> Boolean), needUpdate: ((localItem: T, serverItem: T) -> Boolean)) {
        if (null == list || list.isEmpty() || itemType != LIST) return
        // 保存原始服务器列表（用于删除判断，不修改）
        val originalServerList = list
        // 可修改的服务器列表（用于筛选新增元素）
        val mutableServerList = list.toMutableList()
        // 本地原有数据（副本，避免修改原始数据）
        val localList = data.toMutableList()
        // 处理「更新+保留」的元素（服务器和本地都有同一标识）
        val updatePairs = mutableListOf<Pair<Int, T>>() // <本地元素下标，服务器新元素>
        // 用迭代器安全遍历，避免遍历中删除导致漏元素
        val serverIterator = mutableServerList.iterator()
        while (serverIterator.hasNext()) {
            val serverItem = serverIterator.next()
            // 查找本地是否有同一标识的元素
            val localIndex = localList.findIndexOf { localItem ->
                idMatcher(localItem, serverItem)
            }
            if (localIndex != -1) {
                val localItem = localList[localIndex]
                // 判断是否需要更新（有差异才更新，避免无效刷新）
                if (needUpdate(localItem, serverItem)) {
                    updatePairs.add(localIndex to serverItem)
                }
                // 安全删除：用迭代器移除，避免遍历漏元素
                serverIterator.remove()
            }
        }
        // 处理「新增」的元素（服务器有、本地无）→ mutableServerList 中剩余的元素
        val insertItems = mutableServerList
        // 处理「删除」的元素（本地有、服务器无）→ 用原始服务器列表判断（关键修复）
        val deleteIndices = mutableListOf<Int>()
        localList.forEachIndexed { localIndex, localItem ->
            // 查找原始服务器列表是否有同一标识的元素（没有则需要删除）
            val hasMatch = originalServerList.any { serverItem ->
                idMatcher(localItem, serverItem)
            }
            if (!hasMatch) {
                deleteIndices.add(localIndex)
            }
        }
        // 批量删除（倒序删除，防止下标错乱）→ 你的removed方法是正确的，无需修改
        deleteIndices.sortedDescending().forEach { index ->
            removed(index)
        }
        // 批量更新（按本地原有下标更新）→ 你的changed方法是正确的，无需修改
        updatePairs.forEach { (localIndex, serverItem) ->
            changed(localIndex, serverItem)
        }
        // 批量新增（插入到列表末尾）→ 你的insert方法是正确的，无需修改
        if (insertItems.isNotEmpty()) {
            insert(size(), insertItems)
        }
    }

    /**
     * 刷新集合
     */
    fun refresh(list: List<T>?) {
        if (null == list || itemType != LIST) return
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
     * 仅刷新符合条件的item（无需修改Adapter数据）
     * 适用场景：Holder 中部分值来自非 Data Class 数据源（如本地缓存、ViewModel 状态、临时变量），仅需触发重新绑定
     */
    fun changed(func: ((T) -> Boolean)) {
        if (itemType != LIST) return
        data.findIndexOf(func).apply {
            if (this != -1) {
                notifyItemChanged(this)
            }
        }
    }

    /**
     * 仅触发符合条件item的局部刷新（不修改Adapter数据源）
     * 适用场景：数据来自外部依赖（如ViewModel/全局缓存），仅需更新视图特定部分（需在onConvert中处理payloads）
     * @param func 查找条件（匹配单个item，若匹配多个仅刷新第一个）
     * @param payloads 局部刷新标识（建议使用枚举/常量，避免魔法值）
     */
    fun changed(func: ((T) -> Boolean), payloads: MutableList<Any>) {
        // 空payloads无需触发局部刷新
        if (itemType != LIST || payloads.isEmpty()) return
        val index = findIndex(func)
        if (index in data.indices) {
            notifyItemChanged(index, payloads)
        }
    }

    fun changed(func: ((T) -> Boolean), payload: Any) {
        changed(func, mutableListOf(payload))
    }

    /**
     * 查找到符合条件的对象，改变为新的对象并刷新对应item
     * @param func 查找条件
     * @param bean 新数据（非空）
     * @param payloads 局部刷新参数（可选）
     */
    fun changed(func: ((T) -> Boolean), bean: T?, payloads: MutableList<Any>? = null) {
        changed(findIndex(func), bean, payloads)
    }

    fun changed(func: ((T) -> Boolean), bean: T?, payload: Any) {
        changed(findIndex(func), bean, mutableListOf(payload))
    }

    /**
     * 传入下标+新数据，刷新对应item（支持局部刷新）
     * @param position 目标下标
     * @param bean 新数据（非空）
     * @param payloads 局部刷新参数（可选）
     */
    fun changed(position: Int?, bean: T?, payloads: MutableList<Any>? = null) {
        if (null == position || null == bean || itemType != LIST) return
        if (position in data.indices) {
            data.safeSet(position, bean)
            // payloads为空时传null，触发全量绑定（和无payload逻辑一致）
            val finalPayloads = payloads.takeIf { it?.isNotEmpty() == true }
            notifyItemChanged(position, finalPayloads)
        }
    }

    /**
     * 在列表末尾批量插入集合
     */
    fun insert(list: List<T>?) {
        if (null == list || list.isEmpty() || itemType != LIST) return
        val positionStart = size()
        data.addAll(list)
        notifyItemRangeInserted(positionStart, list.safeSize)
    }

    /**
     * 在指定下标批量插入集合
     */
    fun insert(position: Int?, list: List<T>?) {
        if (null == position || null == list || list.isEmpty() || itemType != LIST) return
        // 记录插入前长度（避免数据变化后计算偏差）
        val oldSize = data.safeSize
        if (position < 0 || position > oldSize) return
        data.addAll(position, list)
        notifyItemRangeInserted(position, list.safeSize)
        // 通知后续Item：下标变化，需要重新绑定
        val startPosition = position + list.safeSize // 插入后的下一个位置
        val itemCount = oldSize - position // 后续需要更新的Item数量
        if (itemCount > 0) {
            notifyItemRangeChanged(startPosition, itemCount)
        }
    }

    /**
     * 在指定下标插入单个对象
     * 只有 “插入位置不是末尾” 时，才需要加 notifyItemRangeChanged
     */
    fun insert(position: Int?, item: T?) {
        if (null == position || null == item || itemType != LIST) return
        // 先记录插入前的长度
        val oldSize = data.safeSize
        if (position < 0 || position > oldSize) return
        data.add(position, item)
        notifyItemInserted(position)
        // 通知从插入位置的下一个位置开始，后面所有的 Item 都需要重新绑定，因为位置变了
        val startPosition = position + 1
        // 用旧长度计算：后续需要更新的Item数量
        val itemCount = oldSize - position
        if (itemCount > 0) {
            notifyItemRangeChanged(startPosition, itemCount)
        }
    }

    /**
     * 在列表末尾插入单个对象
     */
    fun insert(item: T?) {
        if (null == item || itemType != LIST) return
        data.add(item)
        notifyItemInserted(size() - 1)
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
    fun removed(position: Int?) {
        if (null == position || itemType != LIST) return
        if (position in data.indices) {
            data.removeAt(position)
            notifyItemRemoved(position)
            // 通知从删除位置开始的所有Item，它们的位置可能发生了变化，需要重新绑定，这里的 itemCount 是 data.safeSize - index，因为删除后 data 的 size 已经减小了 1
            notifyItemRangeChanged(position, size() - position)
        }
    }

    /**
     * 查找并返回符合条件的对象
     */
    fun findItem(func: ((T) -> Boolean)): T? {
        if (itemType != LIST) return null
        val index = data.findIndexOf(func)
        return data.safeGet(index)
    }

    /**
     * 查找到符合条件的对象，返回下标和对象本身，调用notifyItemChanged（position）修改改变的值
     */
    fun findItemOf(func: ((T) -> Boolean)): Pair<Int, T?>? {
        if (itemType != LIST) return null
        val index = data.findIndexOf(func)
        return index to data.safeGet(index)
    }

    /**
     * 查找符合条件的data数据总数
     */
    fun findCount(func: ((T) -> Boolean)): Int {
        if (itemType != LIST) return -1
        return data.filter(func).safeSize
    }

    /**
     * 返回查找到的符合条件的下标
     */
    fun findIndex(func: ((T) -> Boolean)): Int {
        if (itemType != LIST) return -1
        return data.findIndexOf(func)
    }

    /**
     * 根据下标获取对象
     */
    fun item(position: Int?): T? {
        if (position == null || itemType != LIST) return null
        return data.safeGet(position)
    }

    /**
     * 获取当前集合
     * 直接返回 data 引用，外部可能会直接 adapter.list().clear() 或 add()，导致数据和 UI 不同步
     */
    fun list(): MutableList<T> {
//        return data
        // 返回不可变视图
        return Collections.unmodifiableList(data)
    }

    /**
     * 获取当前对象
     */
    fun bean(): T? {
        return t
    }

    /**
     * 获取当前集合长度
     */
    fun size(): Int {
        return if (itemType == LIST) data.safeSize else 1
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