package com.example.common.widget.xrecyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.view.cancelItemAnimator
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.initLinearHorizontal
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.base.binding.BaseQuickAdapter
import com.example.common.base.page.Paging
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.common.widget.xrecyclerview.refresh.OnRefreshListener
import com.example.common.widget.xrecyclerview.refresh.XRefreshLayout
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter

/**
 * author: wyb
 * date: 2017/11/20.
 * <p>
 * 一般自定义view或viewGroup基本上都会去实现onMeasure、onLayout、onDraw方法，还有另外两个方法是onFinishInflate和onSizeChanged。
 * onFinishInflate方法只有在布局文件中加载view实例会回调，如果直接new一个view的话是不会回调的。
 */
@SuppressLint("InflateParams")
class XRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var refresh: XRefreshLayout? = null//刷新控件 类型1才有
    private var refreshType = 0//页面类型(0无刷新-1带刷新)
    private var emptyType = 0//刷新类型（0顶部-1底部-2全部）
    private var refreshDirection = 0//x是否具有空布局（0无-1有）
    var listPag: Paging? = null//将需要的页面工具类传入，用以控制刷新底部
    var empty: EmptyLayout? = null//自定义封装的空布局
    var recycler: DataRecyclerView? = null//数据列表
    var onClick: (() -> Unit)? = null//空布局点击

    init {
        val mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.XRecyclerView)
        refreshType = mTypedArray.getInt(R.styleable.XRecyclerView_refresh, 0)
        refreshDirection = mTypedArray.getInt(R.styleable.XRecyclerView_refreshDirection, 2)
        emptyType = mTypedArray.getInt(R.styleable.XRecyclerView_empty, 0)
        mTypedArray.recycle()
    }

    override fun onDrawView() {
        if (onFinishView()) initRefreshType(refreshType)
    }

    private fun initRefreshType(refreshType: Int) {
        var view: View? = null
        when (refreshType) {
            0 -> {
                view = LayoutInflater.from(context).inflate(R.layout.view_xrecyclerview, null)
                recycler = view.findViewById(R.id.d_rv)
                if (0 != emptyType) {
                    empty = EmptyLayout(context)
                    recycler?.setEmptyView(empty?.setListView(recycler!!))
                    recycler?.setHasFixedSize(true)
                    recycler?.cancelItemAnimator()
                    recycler?.itemAnimator = DefaultItemAnimator()
                    empty?.onRefreshClick = { onClick?.invoke() }
                }
            }
            1 -> {
                view = LayoutInflater.from(context).inflate(R.layout.view_xrecyclerview_refresh, null)
                empty = view.findViewById(R.id.el)
                refresh = view.findViewById(R.id.x_refresh)
                recycler = view.findViewById(R.id.d_rv)
                refresh?.refPag = listPag
                refresh?.setDirection(refreshDirection)
                recycler?.setHasFixedSize(true)
                recycler?.cancelItemAnimator()
                recycler?.itemAnimator = DefaultItemAnimator()
                if (0 != emptyType) {
                    empty?.onRefreshClick = { onClick?.invoke() }
                } else {
                    empty?.gone()
                }
            }
        }
        addView(view)
    }

    /**
     * 类型1的时候才会显示
     */
    fun setEmptyVisibility(visibility: Int) {
        if (refreshType == 1 && 0 != emptyType) empty?.visibility = visibility
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
        val prop1 = ItemDecorationProps(context.dip2px(horizontalSpace.toFloat()), context.dip2px(verticalSpace.toFloat()), hasHorizontalEdge, hasVerticalEdge)
        propMap.put(0, prop1)
        recycler?.addItemDecoration(SCommonItemDecoration(propMap))
    }

    /**
     * 获取适配器
     */
    fun <T : BaseQuickAdapter<*, *>> getAdapter() = recycler?.adapter as T?

    /**
     * 设置横向左右滑动的adapter
     */
    fun <T : BaseQuickAdapter<*, *>> setHorizontalAdapter(adapter: T) = recycler?.initLinearHorizontal(adapter)

    /**
     * 刷新页面监听
     */
    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        if (refreshType == 1) refresh?.onRefreshListener = onRefreshListener
    }

    /**
     * 结束刷新
     */
    fun finishRefreshing() {
        if (refreshType == 1) refresh?.finishRefreshing()
    }

    /**
     * 结束加载更多
     */
    fun finishLoadmore() {
        if (refreshType == 1) refresh?.finishLoadmore()
    }

    /**
     * 同时关闭上下的刷新
     */
    fun finishRefresh() {
        if (refreshType == 1) refresh?.finishRefresh()
    }

    /**
     * 主动刷新
     */
    fun startRefresh() {
        if (refreshType == 1) refresh?.startRefresh()
    }

    /**
     * 主动加载跟多
     */
    fun startLoadMore() {
        if (refreshType == 1) refresh?.startLoadMore()
    }

    /**
     * 修改空布局背景颜色
     */
    fun setEmptyBackgroundColor(color: Int) = empty?.setBackgroundColor(color)

    /**
     * 当数据正在加载的时候显示
     */
    fun showLoading() {
        if (0 != emptyType) {
            setEmptyVisibility(VISIBLE)
            empty?.showLoading()
        }
    }

    /**
     * 当数据为空时(显示需要显示的图片，以及内容字)
     */
    @JvmOverloads
    fun showEmpty(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) {
            setEmptyVisibility(VISIBLE)
            empty?.showEmpty(imgInt, text)
        }
    }

    /**
     * 当数据异常时
     */
    @JvmOverloads
    fun showError(imgInt: Int = -1, text: String? = null) {
        if (0 != emptyType) {
            setEmptyVisibility(VISIBLE)
            empty?.showError(imgInt, text)
        }
    }

}