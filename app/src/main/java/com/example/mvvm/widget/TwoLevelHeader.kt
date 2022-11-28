package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.scwang.smart.refresh.header.listener.OnTwoLevelListener
import com.scwang.smart.refresh.layout.api.RefreshHeader

/**
 * @description 二级刷新
 * 库中使用的二级下拉刷新控件继承的SimpleComponent中的部分
 * 如onInitialized等方法不允许框架外调用
 * @author yan
 */
@SuppressLint("RestrictedApi")
class TwoLevelHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : com.scwang.smart.refresh.header.TwoLevelHeader(context, attrs), RefreshHeader {

    override fun setOnTwoLevelListener(listener: OnTwoLevelListener): TwoLevelHeader {
        super.setOnTwoLevelListener { refreshLayout -> listener.onTwoLevel(refreshLayout) }
        return this
    }

    override fun setRefreshHeader(header: RefreshHeader?): TwoLevelHeader {
        super.setRefreshHeader(header)
        return this
    }

    override fun setRefreshHeader(header: RefreshHeader?, width: Int, height: Int): TwoLevelHeader {
        super.setRefreshHeader(header, width, height)
        return this
    }

    override fun setMaxRate(rate: Float): TwoLevelHeader {
        super.setMaxRate(rate)
        return this
    }

    override fun setEnablePullToCloseTwoLevel(enabled: Boolean): TwoLevelHeader {
        super.setEnablePullToCloseTwoLevel(enabled)
        return this
    }

    override fun setFloorRate(rate: Float): TwoLevelHeader {
        super.setFloorRate(rate)
        return this
    }

    override fun setRefreshRate(rate: Float): TwoLevelHeader {
        super.setRefreshRate(rate)
        return this
    }

    override fun setEnableTwoLevel(enabled: Boolean): TwoLevelHeader {
        super.setEnableTwoLevel(enabled)
        return this
    }

    override fun setFloorDuration(duration: Int): TwoLevelHeader {
        super.setFloorDuration(duration)
        return this
    }

}