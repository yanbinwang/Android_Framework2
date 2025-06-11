package com.example.common.widget.xrecyclerview.refresh

import android.view.ViewGroup
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

//------------------------------------刷新控件扩展函数类------------------------------------
/**
 * 初始化刷新控件
 */
fun SmartRefreshLayout?.init(listener: OnRefreshLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
    this ?: return
    if (null != header) setRefreshHeader(header)
    if (null != footer) setRefreshFooter(footer)
    setEnableRefresh(true)
    setEnableLoadMore(true)
    if (listener != null) setOnRefreshLoadMoreListener(listener)
    setHeaderAndFooterHeight()
//    applyFullScreen()
}

fun SmartRefreshLayout?.init(onRefresh: OnRefreshListener? = null, onLoadMore: OnLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
    this ?: return
    if (null != header) setRefreshHeader(header)
    if (null != footer) setRefreshFooter(footer)
    if (onRefresh != null) {
        setOnRefreshListener(onRefresh)
        setEnableRefresh(true)
        setEnableScrollContentWhenRefreshed(false)
    } else {
        setEnableRefresh(false)
    }
    if (onLoadMore != null) {
        setOnLoadMoreListener(onLoadMore)
        setEnableLoadMore(true)
        setEnableScrollContentWhenLoaded(false)
    } else {
        setEnableLoadMore(false)
    }
    setHeaderAndFooterHeight()
//    applyFullScreen()
}

/**
 * 完成刷新
 */
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

/**
 * 获取自动刷新的delay时间，如果是手动拖拽，会是false
 * launch {
 *   delay(mRefresh.getAutoRefreshTime())
 *   flow {
 *       emit(request({ CommonApi.instance.getUserInfoApi() }))
 *   }.withHandling(end = {
 *       reset(false)
 *       location.postValue(Unit)
 *   }).collect {
 *       AccountHelper.refresh(it)
 *   }
 * }.manageJob()
 */
fun SmartRefreshLayout?.getAutoRefreshTime(): Long {
    this ?: return 0
    return if (autoRefreshAnimationOnly().orFalse) 300 else 0
}

/**
 * 刷新控件状态
 */
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

/**
 * 没有更多数据初始化
 */
fun SmartRefreshLayout?.noMoreOnInit() {
    this ?: return
    setEnableLoadMore(true)
    applyToHeaderAndFooter { _, footer ->
        footer?.setNoMoreData(true)
    }
}

/**
 * 设置后需要手动在xml中禁用是否加载更多
 * app:srlEnableLoadMore="false"
 * app:srlEnableRefresh="true"
 */
fun SmartRefreshLayout?.setHeaderMaxDragRate() {
    this ?: return
    applyToHeaderAndFooter { header, _ ->
        header?.apply {
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
fun SmartRefreshLayout?.setHeaderAndFooterHeight(headerHeight: Int = 40.pt, footerHeight: Int = 40.pt) {
    this ?: return
    applyToHeaderAndFooter { header, footer ->
        header?.view?.size(MATCH_PARENT, headerHeight)
        footer?.view?.size(MATCH_PARENT, footerHeight)
    }
}

/**
 * 设置顶部/底部的颜色
 */
fun SmartRefreshLayout?.setProgressTint(@ColorRes color: Int) {
    this ?: return
    applyToHeaderAndFooter { header, footer ->
        header?.setProgressTint(color)
        footer?.setProgressTint(color)
    }
}

/**
 * 下拉时候头尾监听
 */
fun SmartRefreshLayout?.setHeaderDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
    this ?: return
    applyToHeaderAndFooter { header, _ ->
        header?.onDragListener = listener
    }
}

fun SmartRefreshLayout?.setFooterDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
    this ?: return
    applyToHeaderAndFooter { _, footer ->
        footer?.onDragListener = listener
    }
}

/**
 * 封装对 refreshHeader 和 refreshFooter 的操作
 */
inline fun <T : SmartRefreshLayout> T.applyToHeaderAndFooter(crossinline action: (header: ProjectRefreshHeader?, footer: ProjectRefreshFooter?) -> Unit) {
    doOnceAfterLayout {
        val header = refreshHeader as? ProjectRefreshHeader
        val footer = refreshFooter as? ProjectRefreshFooter
        action(header, footer)
    }
}

///**
// * 全屏
// */
//fun SmartRefreshLayout?.applyFullScreen() {
//    this ?: return
//    // 获取刷新控件的父容器（可以是任何类型的布局）
//    val parentView = parent as? ViewGroup ?: return
//    // 在父容器布局完成后调整刷新控件大小
//    parentView.doOnceAfterLayout { container ->
//        // 强制刷新控件使用父容器的高度
//        layoutParams = layoutParams.apply {
//            height = container.height
//        }
//    }
//}