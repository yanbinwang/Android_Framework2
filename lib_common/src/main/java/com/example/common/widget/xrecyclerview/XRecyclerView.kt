package com.example.common.widget.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.databinding.ViewDataBinding
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
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.getHolder
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.initConcat
import com.example.framework.utils.function.view.initGridVertical
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.utils.function.view.size
import com.example.framework.widget.BaseViewGroup
import com.example.framework.widget.ObserverRecyclerView
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
class XRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
//    private var emptyEnum = 0//是否具有空布局（0无-1有）
//    private var refreshEnum = 0//页面类型(0无刷新-1带刷新)
    private var refreshEnable = false//是否具有刷新
    private var emptyEnable = false//是否具有空布局
    private var listener: ((result: Boolean) -> Unit)? = null//空布局点击
    var recycler: ObserverRecyclerView? = null//数据列表
        private set
    var refresh: SmartRefreshLayout? = null//刷新控件 类型1才有
        private set
    var empty: EmptyLayout? = null//自定义封装的空布局
        private set

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.XRecyclerView)
//        refreshEnum = typedArray.getInt(R.styleable.XRecyclerView_xrv_refresh_enum, 0)
//        emptyEnum = typedArray.getInt(R.styleable.XRecyclerView_xrv_empty_enum, 0)
        refreshEnable = typedArray.getBoolean(R.styleable.XRecyclerView_xrvEnableRefresh,false)
        emptyEnable = typedArray.getBoolean(R.styleable.XRecyclerView_xrvEnableEmpty,false)
        typedArray.recycle()
    }

    override fun onInflate() {
        if (isInflate) initInflate()
    }

    private fun initInflate() {
        var view: View? = null
        when (refreshEnable) {
            true -> {
                view = context.inflate(R.layout.view_xrecycler)
                recycler = view.findViewById(R.id.rv_list)
                if (emptyEnable) {
                    empty = EmptyLayout(context)
                    recycler?.setEmptyView(empty?.setListView(recycler).apply {
                        if (minimumHeight > 0) {
                            size(height = minimumHeight)
                        } else {
                            size(MATCH_PARENT, MATCH_PARENT)
                        }
                    })
                    empty?.setOnEmptyRefreshListener { listener?.invoke(it) }
                }
            }
            false -> {
                view = context.inflate(R.layout.view_xrecycler_refresh)
                empty = view.findViewById(R.id.empty)
                refresh = view.findViewById(R.id.refresh)
                recycler = view.findViewById(R.id.rv_list)
                if (emptyEnable) {
                    empty?.setOnEmptyRefreshListener { listener?.invoke(it) }
                } else {
                    empty?.gone()
                    view.findViewById<FrameLayout>(R.id.fl_root).apply {
                        if(getChildAt(1) is EmptyLayout) removeViewAt(1)
                    }
                }
            }
        }
        recycler?.setHasFixedSize(true)
        recycler?.cancelItemAnimator()
        addView(view)
        view.size(MATCH_PARENT, WRAP_CONTENT)
    }

    /**
     * 设置默认recycler的输出manager
     * 默认一行一个，线样式可自画可调整
     */
    fun <T : BaseQuickAdapter<*, *>> setAdapter(adapter: T, spanCount: Int = 1, horizontalSpace: Int = 0, verticalSpace: Int = 0, hasHorizontalEdge: Boolean = false, hasVerticalEdge: Boolean = false) {
        recycler.initGridVertical(adapter, spanCount)
        addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge)
    }

    /**
     * 设置复杂的多个adapter直接拼接成一个
     * recycler.layoutManager = GridLayoutManager(recycler.context, 3).apply {
     *     spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
     *         override fun getSpanSize(position: Int): Int {
     *             return when (getItemViewType(position)) {
     *                 TYPE_HEADER -> 3
     *                 TYPE_BODY -> 3
     *                 else -> 1
     *             }
     *          }
     *     }
     * }
     */
    fun <T : BaseQuickAdapter<*, *>> setConcatAdapter(vararg adapters: T) {
        recycler?.initConcat(*adapters)
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
     * 获取一个列表中固定下标的holder
     */
    fun <K : BaseViewDataBindingHolder> getHolder(position: Int): K? {
        return recycler?.getHolder(position)
    }

    fun <VDB : ViewDataBinding> getViewHolder(position: Int): VDB? {
        return getHolder<BaseViewDataBindingHolder>(position)?.getBinding() as? VDB
    }

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
     * 刷新的一些操作
     */
    fun setHeaderMaxDragRate() {
        refresh?.setHeaderMaxDragRate()
    }

    fun setProgressTint(@ColorRes color: Int) {
        refresh?.setProgressTint(color)
    }

    fun setHeaderDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
        refresh?.setHeaderDragListener(listener)
    }

    fun setFooterDragListener(listener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)) {
        refresh?.setFooterDragListener(listener)
    }

    /**
     * 自动触发刷新
     */
    fun autoRefresh() {
        refresh?.autoRefresh()
    }

    /**
     * 结束刷新
     * noMoreData是否有更多数据
     */
    fun finishRefreshing(noMoreData: Boolean? = true) {
        refresh?.finishRefreshing(noMoreData)
    }

    /**
     * 设置空布局点击
     */
    fun setOnEmptyRefreshListener(listener: ((result: Boolean) -> Unit)) {
        this.listener = listener
    }

    /**
     * 修改空布局背景颜色
     */
    fun setEmptyBackgroundColor(color: Int) = empty?.setBackgroundColor(color)

    /**
     * 当数据正在加载的时候显示
     */
    fun loading() {
        empty?.loading()
    }

    /**
     * 当数据为空时(显示需要显示的图片，以及内容字)
     */
    fun empty(resId: Int? = null, resText: Int? = null, resRefreshText: Int? = null, width: Int? = null, height: Int? = null) {
        empty?.empty(resId, resText, resRefreshText, width, height)
    }

    /**
     * 当数据异常时
     */
    fun error(resId: Int? = null, resText: Int? = null, resRefreshText: Int? = null, width: Int? = null, height: Int? = null) {
        empty?.error(resId, resText, resRefreshText, width, height)
    }

}