package com.example.common.widget.xrecyclerview.refresh

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.ColorRes
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/**
 * @description
 * @author 刷新控件
 */
fun SmartRefreshLayout?.init(listener: OnRefreshLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
    this ?: return
    if (null != header) setRefreshHeader(header)
    if (null != footer) setRefreshFooter(footer)
    setEnableRefresh(true)
    setEnableLoadMore(true)
    if (listener != null) setOnRefreshLoadMoreListener(listener)
}

fun SmartRefreshLayout?.init(onRefresh: OnRefreshListener? = null, onLoadMore: OnLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
    this ?: return
    if (null != header) setRefreshHeader(header)
    if (null != footer) setRefreshFooter(footer)
    if (onRefresh != null) {
        setOnRefreshListener(onRefresh)
        setEnableRefresh(true)
    } else {
        setEnableRefresh(false)
    }
    if (onLoadMore != null) {
        setOnLoadMoreListener(onLoadMore)
        setEnableLoadMore(true)
    } else {
        setEnableLoadMore(false)
    }
}

fun SmartRefreshLayout?.finishRefreshing(noMoreData: Boolean? = true) {
    this ?: return
    when (this.state) {
        RefreshState.Loading, RefreshState.LoadFinish, RefreshState.LoadReleased -> {
            finishLoadMore(300)
            if (noMoreData.orFalse) setNoMoreData(true)
        }
        RefreshState.Refreshing, RefreshState.RefreshFinish, RefreshState.RefreshReleased -> {
            finishRefresh()
            if (noMoreData.orFalse) setNoMoreData(true)
        }
        else -> {
            finishLoadMore(300)
            finishRefresh()
            setNoMoreData(noMoreData.orFalse)
        }
    }
}

fun SmartRefreshLayout?.isRefreshing(): Boolean {
    this ?: return false
    return when (this.state) {
        RefreshState.Refreshing, RefreshState.RefreshReleased -> true
        else -> false
    }
}

fun SmartRefreshLayout?.isLoading(): Boolean {
    this ?: return false
    return when (this.state) {
        RefreshState.Loading, RefreshState.LoadReleased, RefreshState.Refreshing, RefreshState.RefreshReleased -> true
        else -> false
    }
}

fun SmartRefreshLayout?.noMoreOnInit() {
    this ?: return
    setEnableLoadMore(true)
    doOnceAfterLayout {
        (refreshFooter as? ProjectRefreshFooter)?.setNoMoreData(true)
    }
}

/**
 * 设置后需要手动在xml中禁用是否加载更多
 * app:srlEnableLoadMore="false"
 * app:srlEnableRefresh="true"
 */
fun SmartRefreshLayout?.setHeaderMaxDragRate() {
    this ?: return
    doOnceAfterLayout {
        (refreshHeader as? ProjectRefreshHeader)?.apply {
            val statusHeight = getStatusBarHeight()
            padding(top = statusHeight)
            setStatusBarHeight(statusHeight)
            val height = 40.ptFloat
            /**
             * 设置下拉最大高度和Header高度的比率（将会影响可以下拉的最大高度）
             * rate – ratio = (the maximum height to drag header)/(the height of header) 比率 = 下拉最大高度 / Header的高度
             */
            setHeaderMaxDragRate(height * 2.5f / (statusHeight + height))
        }
    }
}

/**
 * 设置顶部/底部的高->内部设置是没用的
 */
fun SmartRefreshLayout?.setRefreshHeight(headerHeight: Int = 40.pt, footerHeight: Int = 40.pt) {
    this ?: return
    doOnceAfterLayout {
        (refreshHeader as? ProjectRefreshHeader)?.view.size(MATCH_PARENT, headerHeight)
        (refreshFooter as? ProjectRefreshFooter)?.view.size(MATCH_PARENT, footerHeight)
    }
}

/**
 * 设置顶部/底部的颜色
 */
fun SmartRefreshLayout?.setProgressTint(@ColorRes color: Int) {
    this ?: return
    doOnceAfterLayout {
        (refreshHeader as? ProjectRefreshHeader)?.setProgressTint(color)
        (refreshFooter as? ProjectRefreshFooter)?.setProgressTint(color)
    }
}

fun SmartRefreshLayout?.setHeaderDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
    this ?: return
    doOnceAfterLayout {
        (refreshHeader as? ProjectRefreshHeader)?.onDragListener = listener
    }
}

fun SmartRefreshLayout?.setFooterDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
    this ?: return
    doOnceAfterLayout {
        (refreshFooter as? ProjectRefreshFooter)?.onDragListener = listener
    }
}