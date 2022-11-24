package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.scwang.smart.refresh.header.listener.OnTwoLevelListener
import com.scwang.smart.refresh.layout.api.RefreshHeader

/**
 * @description 自定义头部
 * @author yan
 * https://www.gaitubao.com/xuanzhuan/
 * 默认情况下采用逐帧可以控制动画的开始和停止展现上更好，
 * 如果ui不提供对应图片，手机端去对应网站45°生成8张旋转逐帧图
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