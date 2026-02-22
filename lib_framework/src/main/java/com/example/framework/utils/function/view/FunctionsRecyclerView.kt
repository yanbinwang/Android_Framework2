package com.example.framework.utils.function.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framework.utils.function.doOnDestroy
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
inline fun RecyclerView?.withTempDisabledCache(func: () -> Unit) {
    this ?: return
    // 保存当前动画状态
    val originalAnimations = (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations ?: true
    // 临时禁用缓存
    disableViewHolderCache()
    // 执行具体操作（如刷新数据）
    func()
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
 * RecyclerView 安全更新扩展函数
 * 解决 "Cannot call this method while RecyclerView is computing a layout or scrolling" 异常
 * @param func 要执行的更新逻辑（如 adapter.notifyXXX()/数据修改）
 */
inline fun RecyclerView?.safeUpdate(crossinline func: () -> Unit) {
    // 空指针防护：RecyclerView 实例为空时直接返回
    this ?: return
    // 生命周期防护：View未挂载到窗口时不执行（避免页面销毁后仍更新）
    if (!isAttachedToWindow) return
    // 延迟执行：将更新任务放入主线程消息队列，避开布局计算/滚动周期
    post {
        // 双重校验：执行前再次确认状态（防止post期间View状态变化）
        if (isAttachedToWindow && !isComputingLayout) {
            try {
                func()
            } catch (e: Exception) {
                // 捕获意外异常，避免崩溃扩散（保留基础异常防护）
                e.printStackTrace()
            }
        }
    }
}

/**
 * 获取滑动出来的第一个item的下标
 * (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition().orZero
 */
fun RecyclerView?.addOnScrollLayoutManagerListener(owner: LifecycleOwner? = getLifecycleOwner(), func: (manager: RecyclerView.LayoutManager?) -> Unit = {}) {
    if (this == null) return
    val listener = object : RecyclerView.OnScrollListener() {
        // 滚动状态变化时（停止/拖拽/惯性滚动）回调
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            func.invoke(recyclerView.layoutManager)
        }

        // 滚动过程中持续回调
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            func.invoke(recyclerView.layoutManager)
        }
    }
    addOnScrollListener(listener)
    owner.doOnDestroy {
        removeOnScrollListener(listener)
    }
}

/**
 * GridLayoutManager 是 LinearLayoutManager 的子类，而两者都实现了 LinearLayoutManagerCompat 相关特性
 */
fun RecyclerView.LayoutManager?.getFirstVisibleItemPosition(): Int {
    return if (this is LinearLayoutManager) {
        this.findFirstVisibleItemPosition().orZero
    } else {
        0
    }
}

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
 * 滚动RecyclerView
 */
fun RecyclerView?.smoothScroll(pos: Int, type: Int, scale: Float) {
    if (this == null) return
    (parent as? ViewGroup).actionCancel()
    val layoutManager = layoutManager as? LinearLayoutManager ?: return
    val first = layoutManager.findFirstVisibleItemPosition()
    val last = layoutManager.findLastVisibleItemPosition()
    if (pos !in first..last) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = type
            override fun getHorizontalSnapPreference(): Int = type
        }
        smoothScroller.targetPosition = pos
        layoutManager.startSmoothScroll(smoothScroller)
    } else {
        val targetView = layoutManager.findViewByPosition(pos) ?: return
        val top = targetView.top.orZero
        val height = targetView.height.orZero
        val listHeight = measuredHeight
        when (type) {
            LinearSmoothScroller.SNAP_TO_START -> smoothScrollBy(0, (top * scale).toSafeInt())
            LinearSmoothScroller.SNAP_TO_END -> smoothScrollBy(0, ((top + height - listHeight) * scale).toSafeInt())
        }
    }
}

/**
 * 目标位置置顶
 */
fun RecyclerView?.toPositionSmooth(pos: Int, scale: Float = 1f) {
    smoothScroll(pos, LinearSmoothScroller.SNAP_TO_START, scale)
}

/**
 * 目标位置置底
 */
fun RecyclerView?.toBottomPositionSmooth(pos: Int, scale: Float = 1f) {
    smoothScroll(pos, LinearSmoothScroller.SNAP_TO_END, scale)
}

/**
 * 滚动RecyclerView，可带有offset
 */
fun RecyclerView?.toPosition(pos: Int, offset: Int = 0) {
    if (this == null) return
    // 取消触摸事件
    (parent as? ViewGroup).actionCancel()
    // 滑动到指定位置
    scrollToPosition(pos)
    // 合并 LinearLayoutManager + GridLayoutManager（父类覆盖子类，消除重复分支） 仅保留两个分支，消除冗余判断
    layoutManager?.let { lm ->
        when (lm) {
            is LinearLayoutManager -> lm.scrollToPositionWithOffset(pos, offset)
            is StaggeredGridLayoutManager -> lm.scrollToPositionWithOffset(pos, offset)
            else -> Unit
        }
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