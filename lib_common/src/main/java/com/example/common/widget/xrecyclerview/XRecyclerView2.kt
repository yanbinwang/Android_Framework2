package com.example.common.widget.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.GridLayoutManager
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.pt
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.common.widget.xrecyclerview.refresh.init
import com.example.common.widget.xrecyclerview.refresh.setFooterDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderMaxDragRate
import com.example.common.widget.xrecyclerview.refresh.setProgressTint
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.getHolder
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.utils.function.view.size
import com.example.framework.widget.BaseViewGroup
import com.example.framework.widget.DataRecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/**
 * author: wyb
 * date: 2017/11/20.
 * <p>
 * 一般自定义view或viewGroup基本上都会去实现onMeasure、onLayout、onDraw方法，还有另外两个方法是onFinishInflate和onSizeChanged。
 * onFinishInflate方法只有在布局文件中加载view实例会回调，如果直接new一个view的话是不会回调的。
 */
class XRecyclerView2 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var emptyType = 0//是否具有空布局（0无empty-1有empty）
    private var refreshType = 0//页面类型(0外层无刷新-1外层带刷新)
    private var onRefresh: (() -> Unit)? = null//空布局点击
    val recycler by lazy { DataRecyclerView(context) }//数据列表
    val empty by lazy { EmptyLayout(context) }//自定义封装的空布局
    val refresh by lazy { SmartRefreshLayout(context) }//刷新控件 类型1才有

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.XRecyclerView)
        refreshType = typedArray.getInt(R.styleable.XRecyclerView_refresh, 0)
        emptyType = typedArray.getInt(R.styleable.XRecyclerView_empty, 0)
        typedArray.recycle()
        recycler.setHasFixedSize(true)
        recycler.cancelItemAnimator()
        recycler.size(MATCH_PARENT, MATCH_PARENT)
        if (0 != emptyType) {
            empty.setEmptyRefreshListener { onRefresh?.invoke() }
            empty.size(MATCH_PARENT, MATCH_PARENT)
            if (refreshType == 0) recycler.setEmptyView(empty.setListView(recycler))
        }
    }

    override fun onInflateView() {
        if (isInflate()) init(refreshType)
    }

    private fun init(refreshType: Int) {
        var view: View? = null
        when (refreshType) {
            0 -> view = recycler
            1 -> {
                refresh.size(MATCH_PARENT, MATCH_PARENT)
                val layout = FrameLayout(context)
                layout.size(MATCH_PARENT, MATCH_PARENT)
                layout.addView(refresh)
                layout.addView(recycler)
                if (0 != emptyType) layout.addView(empty)
                view = layout
            }
        }
        addView(view)
        view?.size(MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * 设置默认recycler的输出manager
     * 默认一行一个，线样式可自画可调整
     */
    fun <T : BaseQuickAdapter<*, *>> setAdapter(adapter: T, spanCount: Int = 1, horizontalSpace: Int = 0, verticalSpace: Int = 0, hasHorizontalEdge: Boolean = false, hasVerticalEdge: Boolean = false) {
        recycler.layoutManager = GridLayoutManager(context, spanCount)
        recycler.adapter = adapter
        addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge)
    }

    /**
     * 添加分隔线
     */
    fun addItemDecoration(horizontalSpace: Int, verticalSpace: Int, hasHorizontalEdge: Boolean, hasVerticalEdge: Boolean) {
        val propMap = SparseArray<ItemDecorationProps>()
        val prop1 = ItemDecorationProps(horizontalSpace.pt, verticalSpace.pt, hasHorizontalEdge, hasVerticalEdge)
        propMap.put(0, prop1)
        recycler.addItemDecoration(SCommonItemDecoration(propMap))
    }

    /**
     * 设置横向左右滑动的adapter
     */
    fun <T : BaseQuickAdapter<*, *>> setHorizontalAdapter(adapter: T) = recycler.initLinearHorizontal(adapter)

    /**
     * 获取适配器
     */
    fun <T : BaseQuickAdapter<*, *>> getAdapter() = recycler.adapter as? T

    /**
     * 获取一个列表中固定下标的holder
     */
    fun <K : BaseViewDataBindingHolder> getHolder(position: Int): K? {
        return recycler.getHolder(position)
    }

    /**
     * 刷新页面监听
     * 根据传入不同的监听，确定是否具备头和尾，无需在xml中指定
     */
    fun setOnRefreshListener(listener: OnRefreshLoadMoreListener) {
        refresh.init(listener)
    }

    fun setOnRefreshListener(onRefresh: OnRefreshListener? = null, onLoadMore: OnLoadMoreListener? = null) {
        refresh.init(onRefresh, onLoadMore)
    }

    /**
     * 刷新的一些操作
     */
    fun setHeaderMaxDragRate() {
        refresh.setHeaderMaxDragRate()
    }

    fun setProgressTint(@ColorRes color: Int) {
        refresh.setProgressTint(color)
    }

    fun setHeaderDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
        refresh.setHeaderDragListener(listener)
    }

    fun setFooterDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
        refresh.setFooterDragListener(listener)
    }

    /**
     * 自动触发刷新
     */
    fun autoRefresh() {
        refresh.autoRefresh()
    }

    /**
     * 结束刷新
     * noMoreData是否有更多数据
     */
    fun finishRefreshing(noMoreData: Boolean? = true) {
        if (refreshType == 1) refresh.finishRefreshing(noMoreData)
    }

    /**
     * 设置空布局点击
     */
    fun setEmptyRefreshListener(onRefresh: (() -> Unit)) {
        this.onRefresh = onRefresh
    }

    /**
     * 修改空布局背景颜色
     */
    fun setEmptyBackgroundColor(color: Int) = empty.setBackgroundColor(color)

    /**
     * 当数据正在加载的时候显示
     */
    fun loading() {
        if (0 != emptyType) empty.loading()
    }

    /**
     * 当数据为空时(显示需要显示的图片，以及内容字)
     */
    fun empty(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) empty.empty(imgInt, text)
    }

    /**
     * 当数据异常时
     */
    fun error(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) empty.error(imgInt, text)
    }

}