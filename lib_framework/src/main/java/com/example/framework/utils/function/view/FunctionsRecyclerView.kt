package com.example.framework.utils.function.view

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt

//------------------------------------recyclerview扩展函数类------------------------------------

/**
 * 初始化一个recyclerview
 */
fun RecyclerView?.init(hasFixedSize: Boolean = true) {
    this ?: return
    // 设置 android:clipToPadding="false"
    clipToPadding = false
    // 设置 android:overScrollMode="never"
    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    // 设置 android:scrollbars="none"
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    // 告诉 RecyclerView 尺寸是固定的，不会因为 RecyclerView 中数据项的变化（如添加、删除、更新数据）而改变自身的大小。 RecyclerView 在数据改变时，就不需要重新计算自身的尺寸
    setHasFixedSize(hasFixedSize)
    // 清除自带动画
    cancelItemAnimator()
}

/**
 * 触发本身绑定适配器的刷新
 */
@SuppressLint("NotifyDataSetChanged")
fun RecyclerView?.refresh() {
    if (this == null) return
    this.adapter?.notifyDataSetChanged()
}

/**
 * 清空自带的动画
 */
fun RecyclerView?.cancelItemAnimator() {
    this ?: return
    (itemAnimator as? SimpleItemAnimator)?.apply {
        addDuration = 0
        changeDuration = 0
        moveDuration = 0
        removeDuration = 0
        supportsChangeAnimations = false
    }
}

/**
 * RecyclerView 缓存控制扩展函数
 * 适用于需要精确控制资源加载的场景
 */
fun RecyclerView?.disableViewHolderCache() {
    this ?: return
    // 禁用一级缓存（scrap cache）
    setViewCacheExtension(null)
    // 清空复用池（二级缓存）
    recycledViewPool.clear()
    // 禁用变更动画（避免复用导致的视觉问题）
    (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
}

/**
 * 恢复 RecyclerView 默认缓存策略
 */
fun RecyclerView?.enableViewHolderCache() {
    this ?: return
    // 恢复默认缓存策略（一级缓存大小由系统管理）
    setViewCacheExtension(null) // 保持默认值
    // 不需要清空复用池，保持自然回收
    (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = true
}

/**
 * 临时禁用缓存执行操作并自动恢复
 * 它只在执行特定操作（如数据刷新）时临时禁用缓存，操作完成后立即恢复默认缓存策略，实现精确控制与性能优化的平衡
 */
fun RecyclerView?.withTempDisabledCache(block: () -> Unit) {
    this ?: return
    // 保存当前动画状态
    val originalAnimations = (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations ?: true
    // 临时禁用缓存
    disableViewHolderCache()
    // 执行具体操作（如刷新数据）
    block()
    // 恢复缓存策略（在 block 之后执行）
    enableViewHolderCache()
    // 恢复原始动画状态（在 block 之后执行）
    (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = originalAnimations
}
//* // 使用示例
//* recyclerView.withAsyncTempDisabledCache { onComplete ->
//    *     // 启动异步请求
//    *     viewModel.fetchData { result ->
//        *         adapter.updateData(result)
//        *         onComplete()  // 请求完成后调用回调
//        *     }
//    * }
//inline fun RecyclerView?.withTempDisabledCache(crossinline block: (onComplete: () -> Unit) -> Unit) {
//    this ?: return
//    val originalAnimations = (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations ?: true
//    disableViewHolderCache()
//    block {
//        enableViewHolderCache()
//        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = originalAnimations
//    }
//}

/**
 * 判断是否滑到顶端
 */
fun RecyclerView?.isTop(): Boolean {
    if (this == null) return true
    val layoutManager = layoutManager as? LinearLayoutManager ?: return true
    val position = layoutManager.findFirstVisibleItemPosition()
    return if (position <= 0) {
        val firstChild = layoutManager.findViewByPosition(position)
        firstChild?.let { it.top >= 0 }.orTrue
    } else {
        false
    }
}

/**
 * 判断是否滑到底端
 */
fun RecyclerView?.isBottom(): Boolean {
    if (this == null) return true
    val layoutManager = layoutManager as? LinearLayoutManager ?: return true
    val position = layoutManager.findLastVisibleItemPosition()
    return if (position >= adapter?.itemCount.orZero - 1) {
        val lastChild = layoutManager.findViewByPosition(position)
        lastChild?.let { it.bottom <= height }.orTrue
    } else {
        false
    }
}

/**
 * 滚动RecyclerView，目标位置置顶
 */
fun RecyclerView?.toPositionSmooth(pos: Int, scale: Float = 1f) = smoothScroll(pos, LinearSmoothScroller.SNAP_TO_START, scale)

/**
 * 滚动RecyclerView，目标位置置底
 */
fun RecyclerView?.toBottomPositionSmooth(pos: Int, scale: Float = 1f) = smoothScroll(pos, LinearSmoothScroller.SNAP_TO_END, scale)

fun RecyclerView?.smoothScroll(pos: Int, type: Int, scale: Float) {
    if (this == null) return
    val manager = layoutManager
    if (manager !is LinearLayoutManager) return
    try {
        (parent as ViewGroup).dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val first = manager.findFirstVisibleItemPosition()
    val last = manager.findLastVisibleItemPosition()
    if (first > pos || last < pos) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = type
            override fun getHorizontalSnapPreference(): Int = type
        }
        smoothScroller.targetPosition = pos
        layoutManager?.startSmoothScroll(smoothScroller)
    } else {
        val top = manager.findViewByPosition(pos)?.top.orZero
        val height = manager.findViewByPosition(pos)?.height.orZero
        val listHeight = measuredHeight
        when (type) {
            LinearSmoothScroller.SNAP_TO_START -> smoothScrollBy(0, (top * scale).toSafeInt())
            LinearSmoothScroller.SNAP_TO_END -> smoothScrollBy(0, ((top + height - listHeight) * scale).toSafeInt())
        }
    }
}

/**
 * 滚动RecyclerView，可带有offset
 */
fun RecyclerView?.toPosition(pos: Int, offset: Int = 0) {
    if (this == null) return
    try {
        (parent as ViewGroup).dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    scrollToPosition(pos)
    when (val mLayoutManager = layoutManager) {
        is LinearLayoutManager -> mLayoutManager.scrollToPositionWithOffset(pos, offset)
        is GridLayoutManager -> mLayoutManager.scrollToPositionWithOffset(pos, offset)
        is StaggeredGridLayoutManager -> mLayoutManager.scrollToPositionWithOffset(pos, offset)
    }
}

/**
 * RecyclerView添加不遮挡item的padding
 */
fun RecyclerView?.paddingClip(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    if (this == null) return
    clipToPadding = false
    padding(start, top, end, bottom)
}

/**
 * 设置水平的LayoutManager和adapter
 */
fun RecyclerView?.initLinearHorizontal(adapter: RecyclerView.Adapter<*>?): LinearLayoutManager? {
    this ?: return null
    if (adapter != null) this.adapter = adapter
    return LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply { layoutManager = this }
}

/**
 * 设置垂直的LayoutManager和adapter
 */
fun RecyclerView?.initLinearVertical(adapter: RecyclerView.Adapter<*>? = null): LinearLayoutManager? {
    this ?: return null
    if (adapter != null) this.adapter = adapter
    return LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply { layoutManager = this }
}

/**
 * 设置水平的Grid的GridLayoutManager和adapter
 */
fun RecyclerView?.initGridHorizontal(adapter: RecyclerView.Adapter<*>, columns: Int): GridLayoutManager? {
    this ?: return null
    this.adapter = adapter
    return GridLayoutManager(context, columns, RecyclerView.HORIZONTAL, false).apply { layoutManager = this }
}

/**
 * 设置垂直的Grid的GridLayoutManager和adapter
 */
fun RecyclerView?.initGridVertical(adapter: RecyclerView.Adapter<*>, columns: Int): GridLayoutManager? {
    this ?: return null
    this.adapter = adapter
    return GridLayoutManager(context, columns, RecyclerView.VERTICAL, false).apply { layoutManager = this }
}

/**
 * 设置复杂的多个adapter直接拼接成一个
 */
fun RecyclerView?.initConcat(vararg adapters: RecyclerView.Adapter<*>) {
    this ?: return
    layoutManager = LinearLayoutManager(context)
    adapter = ConcatAdapter(ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build(), *adapters)
}

/**
 * 获取holder
 * 1. 只能获取「活跃状态」的 ViewHolder
 * 原生方法仅返回以下两种状态的 ViewHolder，其他情况返回 null：
 * 正在屏幕内显示的 ViewHolder（Item 在可见区域内）；
 * 刚滚出屏幕、暂存在 Scrap 缓存（一级缓存）的 ViewHolder（还没被回收至 RecycledViewPool）。
 * 若 Item 满足以下条件，getHolder 会返回 null：
 * Item 还没创建（比如首次加载时，屏幕外的 Item 未初始化）；
 * Item 已滚出屏幕并被回收至 RecycledViewPool（二级缓存）；
 * Item 已从数据集中删除（比如调用 notifyItemRemoved 后）。
 * 2. 可能返回 “复用后的旧 ViewHolder”
 * RecyclerView 有复用机制：一个 ViewHolder 可能先绑定 position=0，滚出屏幕后复用给 position=10。若在「复用已发生但 onBindViewHolder 未执行完毕」的间隙调用 getHolder(0)，可能返回已复用给 position=10 的 ViewHolder，导致操作错误的 Item。
 * 3. 配置变更后会失效
 * 当屏幕旋转、语言切换等配置变更发生时，RecyclerView 和 Adapter 会重建，原来的 ViewHolder 会被销毁，此时调用 getHolder 会返回 null。
 */
fun <K : RecyclerView.ViewHolder> RecyclerView?.getHolder(position: Int): K? {
    if (this == null) return null
    adapter?.let {
        if (position !in 0 until it.itemCount) {
            return null
        }
    } ?: return null
    return findViewHolderForAdapterPosition(position) as? K
}

/**
 * 获取滑动出来的第一个item的下标
 * (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition().orZero
 */
fun RecyclerView?.addOnScrollFirstVisibleItemPositionListener(onCurrent: ((manager: RecyclerView.LayoutManager?) -> Unit)?) {
    if (this == null) return
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            onCurrent?.invoke(recyclerView.layoutManager)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            onCurrent?.invoke(recyclerView.layoutManager)
        }
    })
}

//fun RecyclerView?.addOnScrollFirstVisibleItemPositionListener2(onCurrent: ((index: Int) -> Unit)?) {
//    if (this == null) return
//    addOnScrollListener(object : RecyclerView.OnScrollListener() {
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            onCurrent?.invoke(getFirstVisibleItemPosition())
//        }
//
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            onCurrent?.invoke(getFirstVisibleItemPosition())
//        }
//    })
//}

fun RecyclerView?.getFirstVisibleItemPosition(): Int {
    if (this == null) return 0
    return when (val mLayoutManager = layoutManager) {
        is LinearLayoutManager -> mLayoutManager.findFirstVisibleItemPosition().orZero
        is GridLayoutManager -> mLayoutManager.findFirstVisibleItemPosition().orZero
        else -> 0
    }
}