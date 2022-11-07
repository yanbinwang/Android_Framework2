package com.example.common.widget.xrecyclerview.refresh

import com.example.base.utils.function.value.orFalse
import com.example.base.utils.function.view.doOnceAfterLayout
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

fun SmartRefreshLayout?.finishRefreshing(noMore: Boolean? = true) {
    this ?: return
    when (this.state) {
        RefreshState.Loading, RefreshState.LoadFinish, RefreshState.LoadReleased -> {
            finishLoadMore()
            if (noMore.orFalse) setNoMoreData(true)
        }
        RefreshState.Refreshing, RefreshState.RefreshFinish, RefreshState.RefreshReleased -> {
            finishRefresh()
            if (noMore.orFalse) setNoMoreData(true)
        }
        else -> {
            finishLoadMore()
            finishRefresh()
            setNoMoreData(noMore.orFalse)
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
        (it.refreshFooter as? ProjectRefreshFooter)?.setNoMoreData(true)
    }
}

//fun SmartRefreshLayout?.statusBarPadding() {
//    this ?: return
//    (refreshFooter as View?)?.padding(bottom = (10f * 40.dp - 40f.dp).toInt())
//    setFooterMaxDragRate(1f)
//    (refreshHeader as ProjectRefreshHeader?)?.apply {
//        padding(top = Constants.STATUS_BAR_HEIGHT)
//        val height = 60.dp
//        setHeaderMaxDragRate(height * 2.5f / (Constants.STATUS_BAR_HEIGHT.toFloat() + height))
//        background = null
//    }
//}