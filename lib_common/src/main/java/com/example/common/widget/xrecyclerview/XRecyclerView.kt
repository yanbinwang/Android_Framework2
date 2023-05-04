package com.example.common.widget.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.utils.function.pt
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.common.widget.xrecyclerview.refresh.init
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.widget.BaseViewGroup
import com.example.framework.widget.DataRecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
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
class XRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var emptyType = 0//是否具有空布局（0无-1有）
    private var refreshType = 0//页面类型(0无刷新-1带刷新)
    private var refresh: SmartRefreshLayout? = null//刷新控件 类型1才有
    val layout: RefreshLayout get() { return refresh as RefreshLayout }//刷新控件
    var recycler: DataRecyclerView? = null//数据列表
        private set
    var empty: EmptyLayout? = null//自定义封装的空布局
        private set
    var onClick: (() -> Unit)? = null//空布局点击
        private set

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.XRecyclerView)
        refreshType = typedArray.getInt(R.styleable.XRecyclerView_refresh, 0)
        emptyType = typedArray.getInt(R.styleable.XRecyclerView_empty, 0)
        typedArray.recycle()
    }

    override fun onInflateView() {
        if (isInflate()) initRefreshType(refreshType)
    }

    private fun initRefreshType(refreshType: Int) {
        var view: View? = null
        when (refreshType) {
            0 -> {
                view = context.inflate(R.layout.view_xrecyclerview)
                recycler = view.findViewById(R.id.d_rv)
                if (0 != emptyType) {
                    empty = EmptyLayout(context)
                    recycler?.setEmptyView(empty?.setListView(recycler))
                    recycler?.setHasFixedSize(true)
                    recycler?.cancelItemAnimator()
                    empty?.onRefresh = { onClick?.invoke() }
                }
            }
            1 -> {
                view = context.inflate(R.layout.view_xrecyclerview_refresh)
                empty = view.findViewById(R.id.el)
                refresh = view.findViewById(R.id.x_refresh)
                recycler = view.findViewById(R.id.d_rv)
                recycler?.setHasFixedSize(true)
                recycler?.cancelItemAnimator()
                if (0 != emptyType) {
                    empty?.onRefresh = { onClick?.invoke() }
                } else {
                    empty?.gone()
                }
            }
        }
        addView(view)
    }

    /**
     * 设置默认recycler的输出manager
     * 默认一行一个，线样式可自画可调整
     */
    fun <T : BaseQuickAdapter<*, *>> setAdapter(adapter: T, spanCount: Int = 1, horizontalSpace: Int = 0, verticalSpace: Int = 0, hasHorizontalEdge: Boolean = false, hasVerticalEdge: Boolean = false) {
        recycler?.layoutManager = GridLayoutManager(context, spanCount)
        recycler?.adapter = adapter
        addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge)
    }

    /**
     * 添加分隔线
     */
    fun addItemDecoration(horizontalSpace: Int, verticalSpace: Int, hasHorizontalEdge: Boolean, hasVerticalEdge: Boolean) {
        val propMap = SparseArray<ItemDecorationProps>()
        val prop1 = ItemDecorationProps(horizontalSpace.pt, verticalSpace.pt, hasHorizontalEdge, hasVerticalEdge)
        propMap.put(0, prop1)
        recycler?.addItemDecoration(SCommonItemDecoration(propMap))
    }

    /**
     * 设置横向左右滑动的adapter
     */
    fun <T : BaseQuickAdapter<*, *>> setHorizontalAdapter(adapter: T) = recycler?.initLinearHorizontal(adapter)

    /**
     * 获取适配器
     */
    fun <T : BaseQuickAdapter<*, *>> getAdapter() = recycler?.adapter as? T

    /**
     * 刷新页面监听
     * 根据传入不同的监听，确定是否具备头和尾，无需在xml中指定
     */
    fun setOnRefreshListener(listener: OnRefreshLoadMoreListener) {
        refresh?.init(listener)
    }

    fun setOnRefreshListener(onRefresh: OnRefreshListener? = null, onLoadMore: OnLoadMoreListener? = null) {
        refresh?.init(onRefresh, onLoadMore)
    }

    /**
     * 自动触发刷新
     */
    fun autoRefresh() {
        refresh?.autoRefresh()
    }

    /**
     * 结束刷新
     */
    fun finishRefreshing(noMoreData: Boolean? = true) {
        if (refreshType == 1) refresh?.finishRefreshing(noMoreData)
    }

    /**
     * 修改空布局背景颜色
     */
    fun setEmptyBackgroundColor(color: Int) = empty?.setBackgroundColor(color)

    /**
     * 当数据正在加载的时候显示
     */
    fun loading() {
        if (0 != emptyType) empty?.loading()
    }

    /**
     * 当数据为空时(显示需要显示的图片，以及内容字)
     */
    @JvmOverloads
    fun empty(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) empty?.empty(imgInt, text)
    }

    /**
     * 当数据异常时
     */
    @JvmOverloads
    fun error(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) empty?.error(imgInt, text)
    }

}