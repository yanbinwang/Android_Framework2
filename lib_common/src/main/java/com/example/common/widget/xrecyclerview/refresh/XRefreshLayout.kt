package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.util.AttributeSet
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.toSafeFloat
import com.example.common.R
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout

/**
 * @description
 * @author 刷新控件
 */
class XRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TwinklingRefreshLayout(context, attrs, defStyleAttr) {
    private val header by lazy { HeaderView(context) }
    private val bottom by lazy { FooterView(context) }

    init {
        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.XRefreshLayout)
        val direction = mTypedArray.getInt(R.styleable.XRefreshLayout_direction, 2)
        mTypedArray.recycle()
        //定义刷新控件的一些属性
        setHeaderHeight(context.dip2px(25f).toSafeFloat())
        setMaxHeadHeight(context.dip2px(30f).toSafeFloat())
        setBottomHeight(context.dip2px(25f).toSafeFloat())
        setMaxBottomHeight(context.dip2px(30f).toSafeFloat())
        setHeaderView(header)
        setBottomView(bottom)
        setDirection(direction)
    }

    /**
     * 控制刷新
     */
    fun setDirection(direction: Int) {
        when (direction) {
            //顶部
            0 -> setEnable(true, false, false)
            //底部
            1 -> setEnable(false, true, false)
            //都有（默认）
            2 -> setEnable(true, true, true)
        }
    }

    private fun setEnable(refresh: Boolean = true, loadMore: Boolean = true, overScroll: Boolean = true) {
        setEnableRefresh(refresh)
        setEnableLoadmore(loadMore)
        setEnableOverScroll(overScroll)
    }

    /**
     * 同时关闭头尾刷新
     */
    fun finishRefresh() {
        finishRefreshing()
        finishLoadmore()
    }

}