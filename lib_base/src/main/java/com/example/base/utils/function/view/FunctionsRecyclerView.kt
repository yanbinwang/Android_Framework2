package com.example.base.utils.function.view

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.base.utils.function.orTrue
import com.example.base.utils.function.orZero

//------------------------------------recyclerview扩展函数类------------------------------------
/**
 * 设置水平的LayoutManager和adapter
 * */
fun RecyclerView?.initLinearHorizontal(adapter: RecyclerView.Adapter<*>?): LinearLayoutManager? {
    this ?: return null
    if (adapter != null) this.adapter = adapter
    return LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply { layoutManager = this }
}

/**
 * 设置垂直的LayoutManager和adapter
 * */
fun RecyclerView?.initLinearVertical(adapter: RecyclerView.Adapter<*>? = null): LinearLayoutManager? {
    this ?: return null
    if (adapter != null) this.adapter = adapter
    return LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply { layoutManager = this }
}

/**
 * 设置水平的Grid的LayoutManager和adapter
 * */
fun RecyclerView?.initGridHorizontal(adapter: RecyclerView.Adapter<*>, columns: Int): GridLayoutManager? {
    this ?: return null
    this.adapter = adapter
    return GridLayoutManager(context, columns, RecyclerView.VERTICAL, false).apply { layoutManager = this }
}

/**
 * 获取holder
 * */
fun <K : RecyclerView.ViewHolder> RecyclerView?.getHolder(position: Int): K? {
    if (this == null) return null
    adapter?.let { adapter ->
        if (position !in 0 until adapter.itemCount) {
            return null
        }
    } ?: return null
    return findViewHolderForAdapterPosition(position) as? K
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
 * 判断是否滑到顶端
 */
fun RecyclerView?.isTop(): Boolean {
    if (this == null) return true
    val layoutManager = layoutManager as LinearLayoutManager? ?: return true
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
    val layoutManager = layoutManager as LinearLayoutManager? ?: return true
    val position = layoutManager.findLastVisibleItemPosition()
    return if (position >= adapter?.itemCount.orZero - 1) {
        val lastChild = layoutManager.findViewByPosition(position)
        lastChild?.let { it.bottom <= height }.orTrue
    } else {
        false
    }
}