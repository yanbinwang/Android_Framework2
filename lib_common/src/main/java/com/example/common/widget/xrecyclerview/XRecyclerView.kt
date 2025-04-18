package com.example.common.widget.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
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
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.getHolder
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
import androidx.core.content.withStyledAttributes

/**
 * author: wyb
 * date: 2017/11/20.
 *
 * 一般自定义view或viewGroup基本上都会去实现onMeasure、onLayout、onDraw方法，还有另外两个方法是onFinishInflate和onSizeChanged。
 * onFinishInflate方法只有在布局文件中加载view实例会回调，如果直接new一个view的话是不会回调的，需要手动调取
 *
 * 在Android中，如果子View的宽度或高度设置为match_parent，而父View的宽度或高度设置为wrap_content，则会出现以下情况：
 * 子View将会占据尽可能多的空间，即整个父View的宽度或高度。
 * 父View的大小将取决于子View的实际尺寸，即父View的大小将足够包含子View的尺寸。
 * 简单来说，match_parent对子View而言等同于fill_parent，意味着子View将尽可能地填充父View的宽度或高度。而wrap_content则表示子View的大小只会是足够包含其内容的大小。
 */
class XRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    //是否具有刷新
    private var refreshEnable = false
    //是否具有空布局
    private var emptyEnable = false
    //空遮罩高度，-1表示为全屏
    private var emptyHeight = -1f
    //空布局点击
    private var listener: ((result: Boolean) -> Unit)? = null
    //自定义封装的空布局
    val empty by lazy { EmptyLayout(context).apply { onInflate() } }
    //数据列表
    var recycler: ObserverRecyclerView? = null
        private set
    //刷新控件 类型1才有
    var refresh: SmartRefreshLayout? = null
        private set
    //整体容器
    var root: View? = null
        private set

    init {
        context.withStyledAttributes(attrs, R.styleable.XRecyclerView) {
            refreshEnable = getBoolean(R.styleable.XRecyclerView_xrvEnableRefresh, false)
            emptyEnable = getBoolean(R.styleable.XRecyclerView_xrvEnableEmpty, false)
            emptyHeight = getDimension(R.styleable.XRecyclerView_xrvEmptyHeight, -1f)
        }
    }

    override fun onInflate() {
        if (isInflate) initInflate()
    }

    private fun initInflate() {
        var view: View? = null
        when (refreshEnable) {
            false -> {
                view = context.inflate(R.layout.view_xrecycler)
                recycler = view.findViewById(R.id.rv_list)
                if (emptyEnable) {
                    recycler?.setEmptyView(empty.setListView(recycler))
                    emptyConfigure()
                }
            }
            true -> {
                view = context.inflate(R.layout.view_xrecycler_refresh)
                refresh = view.findViewById(R.id.refresh)
                recycler = view.findViewById(R.id.rv_list)
                if (emptyEnable) {
                    (view.findViewById<FrameLayout>(R.id.fl_root))?.addView(empty)
                    emptyConfigure()
                }
            }
        }
        recycler?.setHasFixedSize(true)
        recycler?.cancelItemAnimator()
        addView(view)
        root = view
//        rootSize(MATCH_PARENT, WRAP_CONTENT)
    }

    /**
     * 部分empty是有初始大小要求的，不必撑满整个屏幕
     */
    private fun emptyConfigure() {
        if (-1f == emptyHeight) {
            emptySize(MATCH_PARENT, MATCH_PARENT)
        } else {
            emptySize(MATCH_PARENT, emptyHeight.toSafeInt())
        }
        empty.setOnEmptyRefreshListener {
            listener?.invoke(it)
        }
    }

    /**
     * 设定内部view大小的方法
     */
    fun emptySize(width: Int? = null, height: Int? = null) {
        viewSize(empty, width, height)
    }

    fun recyclerSize(width: Int? = null, height: Int? = null) {
        viewSize(recycler, width, height)
    }

    fun rootSize(width: Int? = null, height: Int? = null) {
        viewSize(root, width, height)
    }

    private fun viewSize(view: View?, width: Int? = null, height: Int? = null) {
        view.size(width, height)
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
        return getHolder<BaseViewDataBindingHolder>(position)?.viewBinding() as? VDB
    }

    /**
     * 让列表滚动到对应下标点
     */
    fun scrollToPosition(position: Int) {
        if (position < 0 || position > recycler?.adapter?.itemCount.orZero -1) return
        recycler?.scrollToPosition(position)
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
    fun setEmptyBackgroundColor(color: Int) = empty.setBackgroundColor(color)

    /**
     * 当数据正在加载的时候显示
     */
    fun loading() {
        empty.loading()
    }

    /**
     * 当数据为空时(显示需要显示的图片，以及内容字)
     */
    fun empty(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        empty.empty(resId, text, refreshText, width, height)
    }

    /**
     * 当数据异常时
     */
    fun error(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        empty.error(resId, text, refreshText, width, height)
    }

}