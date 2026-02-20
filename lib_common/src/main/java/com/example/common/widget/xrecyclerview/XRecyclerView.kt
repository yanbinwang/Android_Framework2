package com.example.common.widget.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.RecyclerView
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.utils.function.pt
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.common.widget.xrecyclerview.refresh.init
import com.example.common.widget.xrecyclerview.refresh.setFooterDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragRate
import com.example.common.widget.xrecyclerview.refresh.setProgressTint
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.init
import com.example.framework.utils.function.view.initConcat
import com.example.framework.utils.function.view.initGridHorizontal
import com.example.framework.utils.function.view.initGridVertical
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.utils.function.view.initLinearVertical
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.paddingAll
import com.example.framework.utils.function.view.paddingLtrb
import com.example.framework.utils.function.view.safeUpdate
import com.example.framework.utils.function.view.size
import com.example.framework.widget.BaseViewGroup
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

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
    // 默认开启嵌套滚动
    private var nestedScrollEnabled = false
    // 是否具有刷新
    private var refreshEnable = false
    // 是否具有空布局
    private var emptyEnable = false
    // 空布局是否传递事件
    private var emptyClickableEnable = false
    // 固定高度，-1表示为全屏
    private var rootFixedHeight = -1
    //----------------以下懒加载会在调取时候创建----------------
    // 整体容器->高度随着子child来拉伸
    val root by lazy { FrameLayout(context).apply {
        size(MATCH_PARENT, MATCH_PARENT)
    }}
    // 刷新控件 类型1才有
    val refresh by lazy { SmartRefreshLayout(context).apply {
        layoutParams = SmartRefreshLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }}
    // 自定义封装的空布局->大小会在添加时设置，xml中是MATCH_PARENT
    val empty by lazy { EmptyLayout(context).apply {
        onInflate()
    }}
    // 数据列表，并且配置默认属性
    val recycler by lazy { RecyclerView(context).apply {
        size(MATCH_PARENT, MATCH_PARENT)
        init()
    }}

    init {
        context.withStyledAttributes(attrs, R.styleable.XRecyclerView) {
            nestedScrollEnabled = getBoolean(R.styleable.XRecyclerView_android_nestedScrollingEnabled, false)
            refreshEnable = getBoolean(R.styleable.XRecyclerView_xrvEnableRefresh, false)
            emptyEnable = getBoolean(R.styleable.XRecyclerView_xrvEnableEmpty, false)
            emptyClickableEnable = getBoolean(R.styleable.XRecyclerView_xrvEnableEmptyClickable, false)
            rootFixedHeight = getInt(R.styleable.XRecyclerView_xrvFixedHeight, -1)
        }
    }

    override fun onInflate() {
        if (isInflate) {
            when (refreshEnable) {
                false -> {
                    root.addView(recycler)
                    configureEmptyLayout()
                }
                true -> {
                    root.addView(refresh)
                    refresh.addView(recycler)
                    configureEmptyLayout()
                }
            }
            addView(root)
            // 插入布局后，存在配置的特殊情况，即我可能只想给定一个固定的高度
            if (-1 != rootFixedHeight) {
                setRootSize(height = rootFixedHeight)
            }
            // 嵌套滚动设置
            setNestedScrollingEnabled(nestedScrollEnabled)
            // 取一次内部padding,针对RecyclerView做padding
            val (resolvedStart, resolvedTop, resolvedEnd, resolvedBottom) = paddingLtrb()
            if (resolvedStart == 0  && resolvedTop == 0 && resolvedEnd == 0 &&  resolvedBottom == 0) return
            paddingAll(0)
            root.paddingAll(0)
            recycler.padding(resolvedStart, resolvedTop, resolvedEnd, resolvedBottom)
        }
    }

    /**
     * 部分empty是有初始大小要求的，不必撑满整个屏幕
     */
    private fun configureEmptyLayout() {
        if (emptyEnable) {
            root.addView(empty)
            empty.size(MATCH_PARENT, MATCH_PARENT)
            empty.isClickable = emptyClickableEnable
        }
    }

    /**
     * 重写View自带的是否支持惯性滑动
     * 1) 默认情况下是false
     * 2) 如果外层嵌套ScrollView/NestedScrollView则需要设为false,不然会卡顿
     * 3) 如果外层嵌套CoordinatorLayout+AppBarLayout+Recyclerview,则Recyclerview需要为true,否则会不响应惯性滑动
     */
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        super.setNestedScrollingEnabled(enabled)
        recycler.isNestedScrollingEnabled = enabled
    }

    /**
     * 设置整体布局大小
     */
    fun setRootSize(width: Int? = null, height: Int? = null) {
        root.size(width.pt, height.pt)
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
        refresh.finishRefreshing(noMoreData)
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
    fun setHeaderDragRate(headerHeight: Int = 40.pt) {
        refresh.setHeaderDragRate(headerHeight)
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

    /**
     * 修改空布局背景颜色
     */
    fun setEmptyBackgroundColor(@ColorInt color: Int) {
        empty.setBackgroundColor(color)
    }

    /**
     * 设置空布局点击
     */
    fun setOnEmptyRefreshListener(listener: ((result: Boolean) -> Unit)) {
        empty.setOnEmptyRefreshListener {
            listener.invoke(it)
        }
    }

    /**
     * 设置默认recycler的输出manager
     * 默认一行一个，线样式可自画可调整
     */
    fun setAdapter(adapter: RecyclerView.Adapter<*>, spanCount: Int = 1, @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL) {
        when {
            // 单列 + 垂直 → 垂直线性布局
            spanCount <= 1 && orientation == RecyclerView.VERTICAL -> {
                recycler.initLinearVertical(adapter)
            }
            // 单列 + 水平 → 水平线性布局
            spanCount <= 1 && orientation == RecyclerView.HORIZONTAL -> {
                recycler.initLinearHorizontal(adapter)
            }
            // 多列 + 垂直 → 垂直网格布局
            spanCount > 1 && orientation == RecyclerView.VERTICAL -> {
                recycler.initGridVertical(adapter, spanCount)
            }
            // 多列 + 水平 → 水平网格布局
            spanCount > 1 && orientation == RecyclerView.HORIZONTAL -> {
                recycler.initGridHorizontal(adapter, spanCount)
            }
        }
    }

    /**
     * 设置自定义快速适配器
     */
    fun <T : BaseQuickAdapter<*, *>> setQuickAdapter(adapter: T, spanCount: Int = 1, horizontalSpace: Int = 0, verticalSpace: Int = 0, @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL) {
        setAdapter(adapter, spanCount, orientation)
        val hasHorizontalEdge = horizontalSpace > 0
        val hasVerticalEdge = verticalSpace > 0
        if (horizontalSpace <= 0 && verticalSpace <= 0) return
        addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge)
    }

//    /**
//     * 设置横向左右滑动的adapter
//     */
//    fun <T : BaseQuickAdapter<*, *>> setHorizontalAdapter(adapter: T) {
//        recycler.initLinearHorizontal(adapter)
//    }

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
        recycler.initConcat(*adapters)
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
     * 获取适配器
     */
    fun getAdapter(): RecyclerView.Adapter<*>? {
        return recycler.adapter
    }

    fun <T : BaseQuickAdapter<*, *>> getQuickAdapter(): T? {
        return recycler.adapter as? T
    }

//    /**
//     * 获取一个列表中固定下标的holder
//     */
//    fun <K : BaseViewDataBindingHolder> getHolder(position: Int): K? {
//        return recycler.getHolder(position)
//    }
//
//    fun <VDB : ViewDataBinding> getViewHolder(position: Int): VDB? {
//        return getHolder<BaseViewDataBindingHolder>(position)?.viewBinding() as? VDB
//    }

    /**
     * 让列表滚动到对应下标点
     */
    fun scrollToPosition(position: Int) {
        if (position < 0 || position > recycler.adapter?.itemCount.orZero -1) return
        recycler.scrollToPosition(position)
    }

    /**
     * 安全更新
     */
    fun safeUpdate(action: () -> Unit) {
        recycler.safeUpdate(action)
    }

    /**
     * 修改RecyclerView背景颜色
     */
    fun setRecyclerBackgroundColor(@ColorInt color: Int) {
        recycler.setBackgroundColor(color)
    }

    /**
     * 判断当前模式
     */
    fun isRefreshEnabled(): Boolean {
        return refreshEnable
    }

    fun isEmptyEnabled(): Boolean {
        return emptyEnable
    }

}