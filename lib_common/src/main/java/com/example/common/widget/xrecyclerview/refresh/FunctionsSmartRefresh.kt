package com.example.common.widget.xrecyclerview.refresh

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.ColorRes
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
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
 * 传统列表页，刷新+加载更多
 */
fun SmartRefreshLayout?.setupRefreshLoadMore(listener: OnRefreshLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
    this ?: return
    if (null != header) setRefreshHeader(header)
    if (null != footer) setRefreshFooter(footer)
    setEnableRefresh(true)
    setEnableLoadMore(true)
    if (listener != null) setOnRefreshLoadMoreListener(listener)
    setHeaderAndFooterHeight()
}

/**
 * 需要分别控制刷新/加载
 */
fun SmartRefreshLayout?.setupRefreshAndLoadMore(onRefresh: OnRefreshListener? = null, onLoadMore: OnLoadMoreListener? = null, header: RefreshHeader? = null, footer: RefreshFooter? = null) {
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
}

/**
 * 只需下拉刷新，无需加载更多
 */
fun SmartRefreshLayout?.setupPullRefresh(listener: OnRefreshListener? = null, header: RefreshHeader? = null, immersive: Boolean = false) {
    this ?: return
    setupRefreshAndLoadMore(onRefresh = listener, header = header)
    if (immersive) correctImmersiveDragRate()
}

/**
 * 只需上拉加载，无需下拉刷新
 */
fun SmartRefreshLayout?.setupLoadMore(listener: OnLoadMoreListener? = null, footer: RefreshFooter? = null) {
    this ?: return
    setupRefreshAndLoadMore(onLoadMore = listener, footer = footer)
}

/**
 * 吸顶头/展开面板 (内容不会被下拉,只会下拉出头部的刷新控件)
 */
fun SmartRefreshLayout?.setupStickyRefresh(listener: OnRefreshListener? = null, header: RefreshHeader? = null, immersive: Boolean = false) {
    this ?: return
    // 是否下拉Header的时候向下平移列表或者内容
    setEnableHeaderTranslationContent(false)
    setupRefreshAndLoadMore(onRefresh = listener, header = header)
    if (immersive) correctImmersiveDragRate()
}

/**
 * 完成刷新
 * true → 标记无更多数据
 * false → 显式重置为“还有更多数据”
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
 * 刷新控件状态
 */
fun SmartRefreshLayout?.isLoading(): Boolean {
    this ?: return false
    return when (this.state) {
        RefreshState.Loading, RefreshState.LoadReleased, RefreshState.Refreshing, RefreshState.RefreshReleased -> true
        else -> false
    }
}

fun SmartRefreshLayout?.isRefreshing(): Boolean {
    this ?: return false
    return when (this.state) {
        RefreshState.Refreshing, RefreshState.RefreshReleased -> true
        else -> false
    }
}

/**
 * 销毁转圈动画资源
 */
fun SmartRefreshLayout?.clearAnimationResources() {
    this ?: return
    applyToHeaderAndFooter { header, footer ->
        header?.release()
        footer?.release()
    }
}

/**
 * 【沉浸式全屏专用】校正下拉拖拽比率
 * 设置后需要手动在xml中禁用是否加载更多
 * app:srlEnableLoadMore="false"
 * app:srlEnableRefresh="true"
 */
fun SmartRefreshLayout?.correctImmersiveDragRate(headerHeight: Int = 40.pt, dragScaleFactor: Float = 2.5f) {
    this ?: return
    applyToHeaderAndFooter { header, _ ->
        // 获取状态栏高度
        val statusHeight = getStatusBarHeight()
        // 设置顶部内边距，让刷新图标避开状态栏
        header.padding(top = statusHeight)
        // 重新设置 View 总高度
        header?.applyStatusBarInset(statusHeight, headerHeight, dragScaleFactor)
        /**
         * 设置下拉最大高度和 Header 高度的比率（影响可以下拉的最大高度）
         * rate -> [比率 = 下拉最大高度 / Header的高度]
         * 1) 由于设置了 paddingTop，SmartRefreshLayout 内部测量的 Header 基准高度已变为 (statusHeight + headerHeight)
         * 2) 为了让【实际最大下拉像素】恰好等于 View 的总高度，必须用“期望总高度”除以“框架内部认知的基准高度”来反推倍率
         */
        val expectedMaxDragPx = statusHeight + headerHeight * dragScaleFactor
        val frameworkBaseHeight = statusHeight + headerHeight
        setHeaderMaxDragRate(expectedMaxDragPx / frameworkBaseHeight.toFloat())
//        setHeaderMaxDragRate(headerHeight * 2.5f / (statusHeight + headerHeight))
    }
}

/**
 * 获取“仅播放下拉动画”模式的持续时长
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
fun SmartRefreshLayout?.getPullAnimDuration(): Long {
    this ?: return 0L
    return if (autoRefreshAnimationOnly().orFalse) 300L else 0L
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
 * 没有更多数据初始化
 */
fun SmartRefreshLayout?.setFooterNoMore() {
    this ?: return
    setEnableLoadMore(true)
    applyToHeaderAndFooter { _, footer ->
        footer?.setNoMoreData(true)
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